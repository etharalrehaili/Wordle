package com.khammin.game.presentation.profile.vm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.Resource
import com.khammin.game.R
import com.khammin.game.data.local.GuestProfileDataStore
import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import com.khammin.game.domain.repository.ChallengeProgressRepository
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import com.khammin.game.presentation.profile.contract.ProfileEffect
import com.khammin.game.presentation.profile.contract.ProfileIntent
import com.khammin.game.presentation.profile.contract.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val localStatsDataStore: LocalStatsDataStore,
    private val guestProfileDataStore: GuestProfileDataStore,
    private val challengeProgressRepository: ChallengeProgressRepository,
    private val challengeDefinitionRepository: ChallengeDefinitionRepository,
    @ApplicationContext private val context: Context,
) : BaseMviViewModel<ProfileIntent, ProfileUiState, ProfileEffect>(
    initialState = ProfileUiState()
) {
    init {
        loadProfile()
        observeAuthState()
    }

    /**
     * Listens for auth state changes. When the user transitions from anonymous
     * to a real Google account, reload the profile and emit a sign-in success effect.
     */
    private fun observeAuthState() {
        var wasAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser ?: return@addAuthStateListener
            val isNowSignedIn = !user.isAnonymous
            if (wasAnonymous && isNowSignedIn) {
                wasAnonymous = false
                loadProfile()
                sendEffect { ProfileEffect.SignedInWithGoogle }
            }
        }
    }

    /** Re-fetches everything — triggered by pull-to-refresh. */
    fun refresh() {
        setState { copy(isRefreshing = true) }
        val perfStart = System.currentTimeMillis()
        Log.d("ProfilePerf", "── START pull-to-refresh")
        loadProfile(perfStart = perfStart, forceRefresh = true)
    }

    private fun loadProfile(perfStart: Long? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                perfStart?.let { Log.d("ProfilePerf", "── FAILED refresh | reason=no user | total=${System.currentTimeMillis() - it}ms") }
                setState { copy(isLoading = false, isRefreshing = false) }
                return@launch
            }
            val uid = user.uid

            if (user.isAnonymous) {
                val saved = guestProfileDataStore.getProfile()
                val displayName = saved?.name?.takeIf { it.isNotBlank() }
                    ?: "GUEST-${uid.take(5).uppercase()}"
                perfStart?.let { Log.d("ProfilePerf", "── DONE refresh (guest/local) | total=${System.currentTimeMillis() - it}ms") }
                setState {
                    copy(
                        name          = displayName,
                        avatarUrl     = saved?.avatarUri,
                        email         = "",
                        isGuest       = true,
                        totalPoints   = localStatsDataStore.getTotalPoints(),
                        enGamesPlayed = localStatsDataStore.getGamesPlayed("en"),
                        enWordsSolved = localStatsDataStore.getWordsSolved("en"),
                        arGamesPlayed = localStatsDataStore.getGamesPlayed("ar"),
                        arWordsSolved = localStatsDataStore.getWordsSolved("ar"),
                        isLoading     = false,
                        isRefreshing  = false,
                    )
                }
                return@launch
            }

            val email = user.email ?: uid

            val fetchStart = System.currentTimeMillis()
            val profile = when (val result = getProfileUseCase(uid, forceRefresh)) {
                is Resource.Success -> result.data ?: run {
                    perfStart?.let { Log.d("ProfilePerf", "── FAILED refresh | reason=null profile | total=${System.currentTimeMillis() - it}ms") }
                    setState { copy(isLoading = false, isRefreshing = false) }
                    return@launch
                }
                is Resource.Error   -> {
                    perfStart?.let { Log.d("ProfilePerf", "── FAILED refresh | reason=${result.message} | total=${System.currentTimeMillis() - it}ms") }
                    setState { copy(isLoading = false, isRefreshing = false) }
                    sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                    return@launch
                }
                else -> {
                    perfStart?.let { Log.d("ProfilePerf", "── FAILED refresh | reason=unexpected state | total=${System.currentTimeMillis() - it}ms") }
                    setState { copy(isLoading = false, isRefreshing = false) }
                    return@launch
                }
            }
            perfStart?.let { Log.d("ProfilePerf", "Profile fetched | step=${System.currentTimeMillis() - fetchStart}ms") }

            val displayName = profile.name.ifBlank { email.substringBefore("@") }

            setState {
                copy(
                    profileId        = profile.id,
                    documentId       = profile.documentId,
                    name             = displayName,
                    email            = email,
                    avatarUrl        = profile.avatarUrl,
                    isGuest          = false,
                    enGamesPlayed    = profile.enGamesPlayed,
                    enWordsSolved    = profile.enWordsSolved,
                    enWinPercentage  = profile.enWinPercentage.toInt(),
                    enCurrentPoints  = profile.enCurrentPoints,
                    arGamesPlayed    = profile.arGamesPlayed,
                    arWordsSolved    = profile.arWordsSolved,
                    arWinPercentage  = profile.arWinPercentage.toInt(),
                    arCurrentPoints  = profile.arCurrentPoints,
                    isLoading        = false,
                    isRefreshing     = false,
                )
            }
            perfStart?.let { Log.d("ProfilePerf", "── DONE refresh | total=${System.currentTimeMillis() - it}ms") }
            loadTotalPoints(uid)
        }
    }

    private fun loadTotalPoints(uid: String) {
        viewModelScope.launch {
            runCatching {
                val definitions = challengeDefinitionRepository.getDefinitions()
                val snapshot    = challengeProgressRepository.getSnapshot(uid)
                val total = definitions
                    .filter { def -> snapshot.challenges[def.id]?.status == ChallengeStatus.COMPLETED }
                    .sumOf { it.points }
                setState { copy(totalPoints = total) }
            }
        }
    }

    override fun onEvent(intent: ProfileIntent) {
        when (intent) {

            ProfileIntent.OnEditProfileClick -> setState {
                copy(isEditMode = true, editName = name)
            }

            ProfileIntent.OnCancelEditClick -> setState {
                copy(isEditMode = false, editName = "")
            }

            is ProfileIntent.OnNameChanged -> setState {
                copy(editName = intent.name)
            }

            is ProfileIntent.OnAvatarChanged -> setState {
                copy(pendingAvatarUri = intent.avatarUri)
            }

            ProfileIntent.OnSaveProfileClick -> {
                val trimmed = uiState.value.editName.trim()

                if (trimmed.isBlank()) {
                    sendEffect { ProfileEffect.ShowError(context.getString(R.string.error_name_empty)) }
                    return
                }

                if (trimmed.length > 25) {
                    sendEffect { ProfileEffect.ShowError(context.getString(R.string.error_name_too_long)) }
                    return
                }

                val state = uiState.value
                val nameChanged   = trimmed != state.name
                val avatarChanged = state.pendingAvatarUri != null

                if (!nameChanged && !avatarChanged) {
                    setState { copy(isEditMode = false, editName = "") }
                    return
                }

                if (state.isGuest) {
                    // Guest: copy avatar to internal storage (stable path) then persist locally
                    viewModelScope.launch {
                        val perfStart = System.currentTimeMillis()
                        Log.d("ProfilePerf", "── START profile update (guest)")
                        setState { copy(isSaving = true) }

                        val avatarUriString = if (state.pendingAvatarUri != null) {
                            val imgStart = System.currentTimeMillis()
                            val result = copyAvatarToInternalStorage(state.pendingAvatarUri) ?: state.avatarUrl
                            Log.d("ProfilePerf", "Image copy done | step=${System.currentTimeMillis() - imgStart}ms")
                            result
                        } else {
                            state.avatarUrl
                        }

                        val writeStart = System.currentTimeMillis()
                        guestProfileDataStore.saveProfile(
                            name        = trimmed,
                            avatarColor = null,
                            avatarEmoji = null,
                            avatarUri   = avatarUriString,
                        )
                        Log.d("ProfilePerf", "Local write done | step=${System.currentTimeMillis() - writeStart}ms")

                        setState {
                            copy(
                                name             = trimmed,
                                avatarUrl        = avatarUriString,
                                pendingAvatarUri = null,
                                isEditMode       = false,
                                editName         = "",
                                isSaving         = false,
                            )
                        }
                        Log.d("ProfilePerf", "── DONE | total=${System.currentTimeMillis() - perfStart}ms")
                        sendEffect { ProfileEffect.ProfileSaved }
                    }
                    return
                }

                // Google user: upload avatar + update Strapi
                viewModelScope.launch {
                    val perfStart = System.currentTimeMillis()
                    Log.d("ProfilePerf", "── START profile update (google)")
                    setState { copy(isSaving = true) }
                    val freshState = uiState.value

                    val avatarUrl = if (freshState.pendingAvatarUri != null) {
                        val imgStart = System.currentTimeMillis()
                        val compressedUri = compressImageUri(context, freshState.pendingAvatarUri)
                        val uploadResult = uploadAvatarUseCase(compressedUri, context)
                        Log.d("ProfilePerf", "Image upload done | step=${System.currentTimeMillis() - imgStart}ms")
                        when (uploadResult) {
                            is Resource.Success -> uploadResult.data
                            is Resource.Error   -> {
                                setState { copy(isSaving = false) }
                                Log.d("ProfilePerf", "── FAILED (upload) | total=${System.currentTimeMillis() - perfStart}ms")
                                sendEffect { ProfileEffect.ShowError(uploadResult.message ?: context.getString(R.string.error_upload_failed)) }
                                return@launch
                            }
                            else -> freshState.avatarUrl
                        }
                    } else {
                        freshState.avatarUrl
                    }

                    val writeStart = System.currentTimeMillis()
                    val result = updateProfileUseCase(
                        documentId    = freshState.documentId,
                        firebaseUid   = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        name          = trimmed,
                        avatarUrl     = avatarUrl,
                        language      = "en",
                        gamesPlayed   = freshState.enGamesPlayed,
                        wordsSolved   = freshState.enWordsSolved,
                        winPercentage = freshState.enWinPercentage.toDouble(),
                        currentPoints = freshState.enCurrentPoints,
                    )
                    Log.d("ProfilePerf", "Firestore write done | step=${System.currentTimeMillis() - writeStart}ms")

                    when (result) {
                        is Resource.Success -> {
                            setState {
                                copy(
                                    name             = trimmed,
                                    avatarUrl        = avatarUrl,
                                    pendingAvatarUri = null,
                                    isEditMode       = false,
                                    editName         = "",
                                    isSaving         = false,
                                )
                            }
                            Log.d("ProfilePerf", "── DONE | total=${System.currentTimeMillis() - perfStart}ms")
                            sendEffect { ProfileEffect.ProfileSaved }
                        }
                        is Resource.Error -> {
                            setState { copy(isSaving = false) }
                            Log.d("ProfilePerf", "── FAILED (write) | total=${System.currentTimeMillis() - perfStart}ms")
                            sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    /**
     * Compresses the image at [uri] to JPEG at 60% quality, scaled down to at most
     * 512×512 px, and writes the result to a temp file in [cacheDir].
     * Returns the URI of the temp file, ready to be passed to the upload use case.
     */
    private suspend fun compressImageUri(context: Context, uri: Uri): Uri =
        withContext(Dispatchers.IO) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val scaled = Bitmap.createScaledBitmap(
                bitmap,
                minOf(bitmap.width, 512),
                minOf(bitmap.height, 512),
                true
            )
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            val bytes = stream.toByteArray()

            val tempFile = File(context.cacheDir, "avatar_upload_temp.jpg")
            tempFile.writeBytes(bytes)
            Log.d("ProfilePerf", "Image compressed | original=${bitmap.byteCount / 1024}KB → compressed=${bytes.size / 1024}KB")
            tempFile.toUri()
        }

    /**
     * Copies the picked image into the app's private files directory so the path
     * remains valid across app restarts (content URIs from the gallery do not).
     * Deletes any previously saved guest avatar to avoid accumulating files.
     */
    private suspend fun copyAvatarToInternalStorage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                // Delete old guest avatar file if one exists
                val existing = guestProfileDataStore.getProfile()?.avatarUri
                if (existing != null) {
                    val oldFile = File(Uri.parse(existing).path ?: "")
                    if (oldFile.exists()) oldFile.delete()
                }

                val inputStream = context.contentResolver.openInputStream(uri) ?: return@runCatching null
                val dest = File(context.filesDir, "guest_avatar.jpg")
                dest.outputStream().use { inputStream.copyTo(it) }
                inputStream.close()
                dest.toUri().toString()
            }.getOrNull()
        }
}
