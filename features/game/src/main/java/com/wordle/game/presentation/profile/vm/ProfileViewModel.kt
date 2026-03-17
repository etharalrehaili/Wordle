package com.wordle.game.presentation.profile.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.util.Resource
import com.wordle.game.R
import com.wordle.game.domain.usecases.profile.CreateProfileUseCase
import com.wordle.game.domain.usecases.profile.GetProfileUseCase
import com.wordle.game.domain.usecases.profile.UpdateProfileUseCase
import com.wordle.game.domain.usecases.profile.UploadAvatarUseCase
import com.wordle.game.presentation.profile.contract.ProfileEffect
import com.wordle.game.presentation.profile.contract.ProfileIntent
import com.wordle.game.presentation.profile.contract.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val createProfileUseCase: CreateProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    @ApplicationContext private val context: Context,
) : BaseMviViewModel<ProfileIntent, ProfileUiState, ProfileEffect>(
    initialState = ProfileUiState()
) {
    init {
        // Load the user's profile as soon as the ViewModel is created
        loadProfile()
    }

    /**
     * Fetches the current user's profile from the backend.
     * If no profile exists yet (e.g. first login), a new one is created automatically
     * using the user's Firebase UID and the local part of their email as a display name.
     */
    private fun loadProfile() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val uid   = user.uid
            val email = user.email ?: uid

            val profile = when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    // Profile found — use it, or create one if the result data is null
                    result.data ?: when (val created = createProfileUseCase(uid, email.substringBefore("@"))) {
                        is Resource.Success -> created.data
                        is Resource.Error   -> {
                            sendEffect { ProfileEffect.ShowError(created.message ?: context.getString(
                                R.string.error_generic)) }
                            return@launch
                        }
                        else -> return@launch
                    }
                }
                is Resource.Error -> {
                    sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                    return@launch
                }
                else -> return@launch
            }

            // If the profile name is not set yet, use the email's local part as a fallback display name
            val displayName = profile.name.ifBlank { email.substringBefore("@") }

            // Update the UI state with the loaded profile data
            setState {
                copy(
                    profileId     = profile.id,
                    documentId    = profile.documentId,
                    name          = displayName,
                    email         = email,
                    avatarUrl     = profile.avatarUrl,
                    gamesPlayed   = profile.gamesPlayed,
                    wordsSolved   = profile.wordsSolved,
                    winPercentage = profile.winPercentage.toInt(),
                    currentPoints = profile.currentPoints,
                )
            }
        }
    }

    override fun onEvent(intent: ProfileIntent) {
        when (intent) {

            // Enter edit mode and initialize the temporary edit name with the current profile name
            ProfileIntent.OnEditProfileClick -> setState {
                copy(isEditMode = true, editName = name)
            }

            // Cancel editing and clear the temporary edit state
            ProfileIntent.OnCancelEditClick -> setState {
                copy(isEditMode = false, editName = "")
            }

            // Update the temporary name field as the user types
            is ProfileIntent.OnNameChanged -> setState {
                copy(editName = intent.name)
            }

            // Store the selected image URI locally until the user saves
            is ProfileIntent.OnAvatarChanged -> setState {
                copy(pendingAvatarUri = intent.avatarUri)
            }

            /**
             * Save profile changes:
             * 1. Validate the display name is not blank
             * 2. Upload the avatar image if a new one was picked
             * 3. Send the updated profile to the backend
             * 4. Update local state on success
             */
            ProfileIntent.OnSaveProfileClick -> {
                val trimmed = uiState.value.editName.trim()
                if (trimmed.isBlank()) {
                    sendEffect { ProfileEffect.ShowError(context.getString(R.string.error_name_empty)) }
                    return
                }

                val state = uiState.value

                // ── Check if anything actually changed ────────────────────
                val nameChanged   = trimmed != state.name
                val avatarChanged = state.pendingAvatarUri != null

                if (!nameChanged && !avatarChanged) {
                    // Nothing changed — just exit edit mode silently
                    setState { copy(isEditMode = false, editName = "") }
                    return
                }

                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    val state = uiState.value

                    // Upload avatar if the user picked a new image, otherwise keep the existing URL
                    val avatarUrl = if (state.pendingAvatarUri != null) {
                        when (val upload = uploadAvatarUseCase(state.pendingAvatarUri, context)) {
                            is Resource.Success -> upload.data
                            is Resource.Error   -> {
                                setState { copy(isLoading = false) }
                                sendEffect { ProfileEffect.ShowError(upload.message ?: context.getString(R.string.error_upload_failed)) }
                                return@launch
                            }
                            else -> state.avatarUrl
                        }
                    } else {
                        state.avatarUrl
                    }

                    // Send the full updated profile to the backend
                    when (val result = updateProfileUseCase(
                        documentId    = state.documentId,
                        name          = trimmed,
                        avatarUrl     = avatarUrl,
                        gamesPlayed   = state.gamesPlayed,
                        wordsSolved   = state.wordsSolved,
                        winPercentage = state.winPercentage.toDouble(),
                        currentPoints = state.currentPoints,
                    )) {
                        is Resource.Success -> {
                            // Apply saved values to the UI state and exit edit mode
                            setState {
                                copy(
                                    name             = trimmed,
                                    avatarUrl        = avatarUrl,
                                    pendingAvatarUri = null,
                                    isEditMode       = false,
                                    editName         = "",
                                    isLoading        = false,
                                )
                            }
                            sendEffect { ProfileEffect.ProfileSaved }
                        }
                        is Resource.Error -> {
                            setState { copy(isLoading = false) }
                            sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(
                                R.string.error_generic)) }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}