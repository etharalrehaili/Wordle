package com.khammin.game.presentation.preferences.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage

data class PreferencesUiState(
    val selectedLanguage: AppLanguage  = AppLanguage.ENGLISH,
    val selectedTheme: AppColorTheme   = AppColorTheme.DARK,
) : UiState

sealed interface PreferencesEffect : UiEffect

sealed interface PreferencesIntent : UiIntent {
    data class ChangeLanguage(val language: AppLanguage) : PreferencesIntent
    data class ChangeTheme(val theme: AppColorTheme)     : PreferencesIntent
}