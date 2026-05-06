package com.khammin.game.presentation.profile.vm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.ByteArrayOutputStream
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
import com.khammin.core.util.Resource
import com.khammin.game.R
import com.khammin.game.data.local.GuestProfileDataStore
import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import com.khammin.game.domain.repository.ChallengeProgressRepository
import com.khammin.game.domain.model.ProfileUpdate
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import com.khammin.game.presentation.profile.contract.ProfileEffect
import com.khammin.game.presentation.profile.contract.ProfileIntent
import com.khammin.game.presentation.profile.contract.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import androidx.core.graphics.scale

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val localStatsDataStore: LocalStatsDataStore,
    private val guestProfileDataStore: GuestProfileDataStore,
    private val challengeProgressRepository: ChallengeProgressRepository,
    private val challengeDefinitionRepository: ChallengeDefinitionRepository,
    private val auth: FirebaseAuth,
    private val networkUtils: NetworkUtils,
    @ApplicationContext private val context: Context,
) : BaseMviViewModel<ProfileIntent, ProfileUiState, ProfileEffect>(
    initialState = ProfileUiState()
) {
    init {
        loadProfile()
        observeAuthState()
    }

    private fun observeAuthState() {
        var wasAnonymous = auth.currentUser?.isAnonymous == true
        auth.addAuthStateListener { auth ->
            val user = auth.currentUser ?: return@addAuthStateListener
            val isNowSignedIn = !user.isAnonymous
            if (wasAnonymous && isNowSignedIn) {
                wasAnonymous = false
                loadProfile()
                sendEffect { ProfileEffect.SignedInWithGoogle }
            }
        }
    }

    fun refresh() {
        setState { copy(isRefreshing = true) }
        loadProfile(forceRefresh = true)
    }

    private suspend fun awaitCurrentUser(): FirebaseUser? = suspendCancellableCoroutine { cont ->
        val auth = auth
        // Fast path — user already known.
        val current = auth.currentUser
        if (current != null) {
            cont.resume(current)
            return@suspendCancellableCoroutine
        }
        // Slow path — Firebase hasn't finished loading the cached session yet.
        // addAuthStateListener fires immediately (or once auth is ready) with the true state.
        var fired = false
        val listener = FirebaseAuth.AuthStateListener { a ->
            if (!fired && cont.isActive) {
                fired = true
                cont.resume(a.currentUser)
            }
        }
        auth.addAuthStateListener(listener)
        cont.invokeOnCancellation { auth.removeAuthStateListener(listener) }
    }

    private fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            try {
                // On refresh, Firebase auth may still be initializing. Wait for the first
                // definitive auth state rather than bailing out with "reason=no user".
                val user: FirebaseUser? = if (forceRefresh && auth.currentUser == null) {
                    awaitCurrentUser()
                } else {
                    auth.currentUser
                }

                if (user == null) {
                    return@launch
                }
                val uid = user.uid

                if (user.isAnonymous) {
                    // Guest path — all data comes from local DataStore (no Firestore involved).
                    val saved = guestProfileDataStore.getProfile()
                    val displayName = saved?.name?.takeIf { it.isNotBlank() }
                        ?: "GUEST-${uid.take(5).uppercase()}"

                    val gamesPlayed = localStatsDataStore.getGamesPlayed("ar")
                    val wordsSolved = localStatsDataStore.getWordsSolved("ar")
                    val totalPoints = localStatsDataStore.getTotalPoints()

                    setState {
                        copy(
                            name          = displayName,
                            avatarUrl     = saved?.avatarUri,
                            email         = "",
                            isGuest       = true,
                            totalPoints   = totalPoints,
                            arGamesPlayed = gamesPlayed,
                            arWordsSolved = wordsSolved,
                        )
                    }

                    // Guest data loads from local storage in <10ms — faster than one frame.
                    // Delay until at least 600ms have elapsed so the pull-to-refresh indicator
                    // is actually visible before isRefreshing flips back to false.
                    if (forceRefresh) {
                        val elapsed = System.currentTimeMillis() - start
                        val remaining = 600L - elapsed
                        if (remaining > 0) delay(remaining)
                    }

                    return@launch
                }

                // Google-account path — stats come from the Firestore-backed profile (cached in Room).
                val email = user.email ?: uid

                val profile = when (val result = getProfileUseCase(uid, forceRefresh)) {
                    is Resource.Success -> result.data ?: run {
                        sendEffect { ProfileEffect.ShowError(context.getString(R.string.error_generic)) }
                        return@launch
                    }
                    is Resource.Error -> {
                        sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                        return@launch
                    }
                    else -> {
                        return@launch
                    }
                }

                val displayName = profile.name.ifBlank { email.substringBefore("@") }

                setState {
                    copy(
                        profileId     = profile.id,
                        documentId    = profile.documentId,
                        name          = displayName,
                        email         = email,
                        avatarUrl     = profile.avatarUrl,
                        isGuest       = false,
                        arGamesPlayed = profile.arGamesPlayed,
                        arWordsSolved = profile.arWordsSolved,
                    )
                }

                loadTotalPoints(uid)

            } finally {
                // Always clear loading indicators — even if an early return or exception occurs.
                setState { copy(isLoading = false, isRefreshing = false) }
            }
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

            ProfileIntent.OnSignInWithGoogleClick -> {
                if (networkUtils.isConnected()) {
                    sendEffect { ProfileEffect.TriggerGoogleSignIn }
                } else {
                    setState { copy(showNoInternet = true) }
                }
            }

            ProfileIntent.DismissNoInternet -> setState { copy(showNoInternet = false) }

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
                        setState { copy(isSaving = true) }

                        val avatarUriString = if (state.pendingAvatarUri != null) {
                            copyAvatarToInternalStorage(state.pendingAvatarUri) ?: state.avatarUrl
                        } else {
                            state.avatarUrl
                        }

                        guestProfileDataStore.saveProfile(
                            name        = trimmed,
                            avatarColor = null,
                            avatarEmoji = null,
                            avatarUri   = avatarUriString,
                        )

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
                        sendEffect { ProfileEffect.ProfileSaved }
                    }
                    return
                }

                // Google user: upload avatar + update Strapi
                viewModelScope.launch {
                    setState { copy(isSaving = true) }
                    val freshState = uiState.value

                    val avatarUrl = if (freshState.pendingAvatarUri != null) {
                        val compressedUri = compressImageUri(context, freshState.pendingAvatarUri)
                        val uploadResult = uploadAvatarUseCase(compressedUri)
                        when (uploadResult) {
                            is Resource.Success -> uploadResult.data
                            is Resource.Error   -> {
                                setState { copy(isSaving = false) }
                                sendEffect { ProfileEffect.ShowError(uploadResult.message ?: context.getString(R.string.error_upload_failed)) }
                                return@launch
                            }
                            else -> freshState.avatarUrl
                        }
                    } else {
                        freshState.avatarUrl
                    }

                    val result = updateProfileUseCase(
                        ProfileUpdate(
                            documentId  = freshState.documentId,
                            firebaseUid = auth.currentUser?.uid ?: "",
                            name        = trimmed,
                            avatarUrl   = avatarUrl,
                            language    = "en",
                        )
                    )

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
                            sendEffect { ProfileEffect.ProfileSaved }
                        }
                        is Resource.Error -> {
                            setState { copy(isSaving = false) }
                            sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private suspend fun compressImageUri(context: Context, uri: Uri): Uri =
        withContext(Dispatchers.IO) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val scaled = bitmap.scale(minOf(bitmap.width, 512), minOf(bitmap.height, 512))
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            val bytes = stream.toByteArray()

            val tempFile = File(context.cacheDir, "avatar_upload_temp.jpg")
            tempFile.writeBytes(bytes)
            tempFile.toUri()
        }

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
