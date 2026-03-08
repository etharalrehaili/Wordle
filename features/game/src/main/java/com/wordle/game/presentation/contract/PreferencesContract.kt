package com.wordle.game.presentation.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage

data class PreferencesUiState(
    val selectedTheme: AppColorTheme = AppColorTheme.DARK,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
) : UiState

sealed interface PreferencesEffect : UiEffect {
    data class ApplyLanguage(val locale: String) : PreferencesEffect
}

sealed class PreferencesIntent : UiIntent {
    data class ChangeTheme(val theme: AppColorTheme) : PreferencesIntent()
    data class ChangeLanguage(val language: AppLanguage) : PreferencesIntent()
}