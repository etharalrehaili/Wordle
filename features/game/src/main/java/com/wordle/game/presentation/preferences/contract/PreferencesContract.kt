package com.wordle.game.presentation.preferences.contract

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage

data class PreferencesUiState(
    val selectedLanguage: AppLanguage  = AppLanguage.ENGLISH,
    val selectedTheme: AppColorTheme   = AppColorTheme.DARK,
) : UiState

sealed interface PreferencesEffect : UiEffect

sealed interface PreferencesIntent : UiIntent {
    data class ChangeLanguage(val language: AppLanguage) : PreferencesIntent
    data class ChangeTheme(val theme: AppColorTheme)     : PreferencesIntent
}