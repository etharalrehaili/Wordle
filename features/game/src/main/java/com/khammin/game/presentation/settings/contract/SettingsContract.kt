package com.khammin.game.presentation.settings.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState

data class SettingsUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val isGuest: Boolean = false,
) : UiState


sealed interface SettingsEffect : UiEffect {
    data object SignOutSuccess           : SettingsEffect
}

sealed class SettingsIntent : UiIntent {
    data object OnSignOutClick        : SettingsIntent()
}
