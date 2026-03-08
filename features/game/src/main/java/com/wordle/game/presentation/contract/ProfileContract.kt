package com.wordle.game.presentation.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class ProfileUiState(
    val name: String          = "",
    val avatarUrl: String?    = null,
    val gamesPlayed: Int      = 0,
    val wordsSolved: Int      = 0,
    val winPercentage: Int    = 0,
    val currentPoints: Int    = 0,
    val isEditMode: Boolean   = false,
    val editName: String      = "",
    val isLoading: Boolean    = false,
) : UiState

sealed interface ProfileEffect : UiEffect {
    data object ProfileSaved : ProfileEffect
    data class ShowError(val message: String) : ProfileEffect
}

sealed class ProfileIntent : UiIntent {
    data object OnEditProfileClick : ProfileIntent()
    data object OnSaveProfileClick : ProfileIntent()
    data object OnCancelEditClick : ProfileIntent()
    data class OnNameChanged(val name: String) : ProfileIntent()
    data class OnAvatarChanged(val avatarUrl: String?) : ProfileIntent()
}
