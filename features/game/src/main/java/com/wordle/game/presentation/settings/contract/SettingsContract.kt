package com.wordle.game.presentation.settings.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState

data class SettingsUiState(
    val isLoading: Boolean = false,
    val email: String = "",
) : UiState

sealed interface SettingsEffect : UiEffect {
    data object NavigateToChangeEmail    : SettingsEffect
    data object NavigateToChangePassword : SettingsEffect
    data object SignOutSuccess           : SettingsEffect
    data class ShowError(val message: String) : SettingsEffect
}

sealed class SettingsIntent : UiIntent {
    data object OnChangeEmailClick    : SettingsIntent()
    data object OnChangePasswordClick : SettingsIntent()
    data object OnSignOutClick        : SettingsIntent()
}