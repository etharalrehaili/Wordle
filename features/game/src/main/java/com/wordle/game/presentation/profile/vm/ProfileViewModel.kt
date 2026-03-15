package com.wordle.game.presentation.profile.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.util.Resource
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
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val uid = user.uid
            val email = user.email ?: uid

            val profile = when (val result = getProfileUseCase(uid)) {
                is Resource.Success -> {
                    result.data ?: when (val created = createProfileUseCase(uid, email.substringBefore("@"))) {                         is Resource.Success -> created.data
                        is Resource.Error -> {
                            sendEffect {
                                ProfileEffect.ShowError(
                                    created.message ?: "Error"
                                )
                            }; return@launch
                        }

                        else -> return@launch
                    }
                }

                is Resource.Error -> {
                    sendEffect { ProfileEffect.ShowError(result.message ?: "Error") }; return@launch
                }

                else -> return@launch
            }

            val displayName = profile.name.ifBlank {
                email.substringBefore("@")
            }

            Log.d("ProfileAvatar", "loadProfile: profile.avatarUrl=${profile.avatarUrl}")
            setState {
                copy(
                    profileId = profile.id,
                    documentId = profile.documentId,
                    name = displayName,
                    email         = email,
                    avatarUrl = profile.avatarUrl,
                    gamesPlayed = profile.gamesPlayed,
                    wordsSolved = profile.wordsSolved,
                    winPercentage = profile.winPercentage.toInt(),
                    currentPoints = profile.currentPoints,
                )
            }
        }
    }

    override fun onEvent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.OnEditProfileClick -> setState {
                copy(
                    isEditMode = true,
                    editName = name
                )
            }

            ProfileIntent.OnCancelEditClick -> setState { copy(isEditMode = false, editName = "") }
            is ProfileIntent.OnNameChanged -> setState { copy(editName = intent.name) }
            is ProfileIntent.OnAvatarChanged -> setState { copy(pendingAvatarUri = intent.avatarUri) }

            ProfileIntent.OnSaveProfileClick -> {
                val trimmed = uiState.value.editName.trim()
                if (trimmed.isBlank()) {
                    sendEffect { ProfileEffect.ShowError("Name cannot be empty") }
                    return
                }
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    val state = uiState.value

                    val avatarUrl = if (state.pendingAvatarUri != null) {
                        when (val upload = uploadAvatarUseCase(state.pendingAvatarUri, context)) {
                            is Resource.Success -> upload.data
                            is Resource.Error -> {
                                Log.e("ProfileAvatar", "OnSaveProfileClick: upload failed ${upload.message}")
                                setState { copy(isLoading = false) }; sendEffect {
                                    ProfileEffect.ShowError(
                                        upload.message ?: "Upload failed"
                                    )
                                }; return@launch
                            }

                            else -> state.avatarUrl
                        }
                    } else {
                        state.avatarUrl
                    }
                    Log.d("ProfileAvatar", "OnSaveProfileClick: avatarUrl to send=$avatarUrl")

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
                            Log.d("ProfileAvatar", "OnSaveProfileClick: update success result.data.avatarUrl=${result.data.avatarUrl} setting state avatarUrl=$avatarUrl")
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
                            sendEffect { ProfileEffect.ShowError(result.message ?: "Error") }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}