package com.wordle.game.presentation.viewmodel

import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.game.presentation.contract.ProfileEffect
import com.wordle.game.presentation.contract.ProfileIntent
import com.wordle.game.presentation.contract.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() :
    BaseMviViewModel<ProfileIntent, ProfileUiState, ProfileEffect>(
        initialState = ProfileUiState(
            name          = "Ahmed Al-Rashid",
            avatarUrl     = null,
            gamesPlayed   = 42,
            wordsSolved   = 35,
            winPercentage = 78,
            currentPoints = 24500,
        )
    ) {

    override fun onEvent(intent: ProfileIntent) {
        when (intent) {

            ProfileIntent.OnEditProfileClick -> setState {
                copy(isEditMode = true, editName = name)
            }

            ProfileIntent.OnCancelEditClick -> setState {
                copy(isEditMode = false, editName = "")
            }

            ProfileIntent.OnSaveProfileClick -> {
                val trimmed = uiState.value.editName.trim()
                if (trimmed.isBlank()) {
                    sendEffect { ProfileEffect.ShowError("Name cannot be empty") }
                    return
                }
                setState { copy(name = trimmed, isEditMode = false, editName = "") }
                sendEffect { ProfileEffect.ProfileSaved }
            }

            is ProfileIntent.OnNameChanged ->
                setState { copy(editName = intent.name) }

            is ProfileIntent.OnAvatarChanged ->
                setState { copy(avatarUrl = intent.avatarUrl) }
        }
    }
}