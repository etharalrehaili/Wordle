package com.wordle.game.presentation.profile.contract

import android.net.Uri
import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class ProfileUiState(
    val profileId: Int = 0,
    val documentId: String = "",
    val name: String          = "",
    val email: String = "",
    val avatarUrl: String?    = null,
    val pendingAvatarUri: Uri? = null,
    val isEditMode: Boolean   = false,
    val editName: String      = "",
    val isLoading: Boolean    = false,
    val enGamesPlayed: Int = 0,
    val enWordsSolved: Int = 0,
    val enWinPercentage: Int = 0,
    val enCurrentPoints: Int = 0,
    val arGamesPlayed: Int = 0,
    val arWordsSolved: Int = 0,
    val arWinPercentage: Int = 0,
    val arCurrentPoints: Int = 0,
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
    data class OnAvatarChanged(val avatarUri: Uri?) : ProfileIntent()
}
