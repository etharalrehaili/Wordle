package com.wordle.game.presentation.profile.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.util.Resource
import com.wordle.game.R
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
     */
    private fun loadProfile() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val uid   = user.uid
            val email = user.email ?: uid

            // Profile is guaranteed to exist by the time the user navigates here
            // (HomeViewModel creates it on login), so just load it
            val profile = when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> result.data ?: return@launch
                is Resource.Error   -> {
                    sendEffect { ProfileEffect.ShowError(result.message ?: context.getString(R.string.error_generic)) }
                    return@launch
                }
                else -> return@launch
            }

            val displayName = profile.name.ifBlank { email.substringBefore("@") }

            setState {
                copy(
                    profileId        = profile.id,
                    documentId       = profile.documentId,
                    name             = displayName,
                    email            = email,
                    avatarUrl        = profile.avatarUrl,
                    // English stats
                    enGamesPlayed    = profile.enGamesPlayed,
                    enWordsSolved    = profile.enWordsSolved,
                    enWinPercentage  = profile.enWinPercentage.toInt(),
                    enCurrentPoints  = profile.enCurrentPoints,
                    // Arabic stats
                    arGamesPlayed    = profile.arGamesPlayed,
                    arWordsSolved    = profile.arWordsSolved,
                    arWinPercentage  = profile.arWinPercentage.toInt(),
                    arCurrentPoints  = profile.arCurrentPoints,
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
                        firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        name          = trimmed,
                        avatarUrl     = avatarUrl,
                        language      = "en",
                        gamesPlayed   = state.enGamesPlayed,
                        wordsSolved   = state.enWordsSolved,
                        winPercentage = state.enWinPercentage.toDouble(),
                        currentPoints = state.enCurrentPoints,
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