package com.wordle.game.presentation.preferences.vm

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.game.presentation.preferences.contract.PreferencesEffect
import com.wordle.game.presentation.preferences.contract.PreferencesIntent
import com.wordle.game.presentation.preferences.contract.PreferencesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : BaseMviViewModel<PreferencesIntent, PreferencesUiState, PreferencesEffect>(
    initialState = PreferencesUiState()
) {
    companion object {
        val THEME_KEY    = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    init {
        viewModelScope.launch {
            dataStore.data.first().let { prefs ->
                val theme    = prefs[THEME_KEY]?.let { AppColorTheme.valueOf(it) } ?: AppColorTheme.DARK
                val language = prefs[LANGUAGE_KEY]?.let { AppLanguage.entries.find { l -> l.code == it } } ?: AppLanguage.ENGLISH
                setState { copy(selectedTheme = theme, selectedLanguage = language) }
            }
        }
    }

    override fun onEvent(intent: PreferencesIntent) {
        when (intent) {
            is PreferencesIntent.ChangeTheme    -> handleThemeChange(intent.theme)
            is PreferencesIntent.ChangeLanguage -> handleLanguageChange(intent.language)
        }
    }

    private fun handleThemeChange(theme: AppColorTheme) {
        setState { copy(selectedTheme = theme) }
        viewModelScope.launch {
            dataStore.edit { it[THEME_KEY] = theme.name }
        }
    }

    private fun handleLanguageChange(language: AppLanguage) {
        setState { copy(selectedLanguage = language) }
        sendEffect { PreferencesEffect.ApplyLanguage(language.code) }
        viewModelScope.launch {
            dataStore.edit { it[LANGUAGE_KEY] = language.code }
        }
    }
}