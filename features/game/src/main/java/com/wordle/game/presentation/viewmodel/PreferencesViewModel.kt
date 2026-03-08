package com.wordle.game.presentation.viewmodel

import com.wordle.core.mvi.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.game.presentation.contract.PreferencesEffect
import com.wordle.game.presentation.contract.PreferencesIntent
import com.wordle.game.presentation.contract.PreferencesUiState

@HiltViewModel
class PreferencesViewModel @Inject constructor(
) : BaseMviViewModel<PreferencesIntent, PreferencesUiState, PreferencesEffect>(
    initialState = PreferencesUiState()
) {
    override fun onEvent(intent: PreferencesIntent) {
        when (intent) {
            is PreferencesIntent.ChangeTheme -> handleThemeChange(intent.theme)
            is PreferencesIntent.ChangeLanguage -> handleLanguageChange(intent.language)
        }
    }

    private fun handleThemeChange(theme: AppColorTheme) {
        setState { copy(selectedTheme = theme) }
    }

    private fun handleLanguageChange(language: AppLanguage) {
        setState { copy(selectedLanguage = language) }
        sendEffect { PreferencesEffect.ApplyLanguage(language.code) }
    }
}