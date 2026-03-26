package com.khammin.game.presentation.preferences.vm

import com.khammin.core.domain.model.DARK_MODEL
import com.khammin.core.domain.model.ENGLISH_MODEL
import com.khammin.core.domain.model.LIGHT_MODEL
import dagger.hilt.android.lifecycle.HiltViewModel
import com.khammin.core.domain.model.LanguageModel
import com.khammin.core.domain.model.ThemeModel
import com.khammin.core.domain.model.isArabic
import com.khammin.core.domain.model.isDark
import com.khammin.core.domain.usecase.GetCurrentLanguageUseCase
import com.khammin.core.domain.usecase.GetCurrentThemeUseCase
import com.khammin.core.domain.usecase.GetLanguageUseCase
import com.khammin.core.domain.usecase.GetThemeUseCase
import com.khammin.core.domain.usecase.SetLanguageUseCase
import com.khammin.core.domain.usecase.SetThemeUseCase
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.game.presentation.preferences.contract.PreferencesEffect
import com.khammin.game.presentation.preferences.contract.PreferencesIntent
import com.khammin.game.presentation.preferences.contract.PreferencesUiState
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val getLanguageUseCase: GetLanguageUseCase,
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val getCurrentThemeUseCase: GetCurrentThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
) : BaseMviViewModel<PreferencesIntent, PreferencesUiState, PreferencesEffect>(
    initialState = PreferencesUiState(
        selectedLanguage = if (getCurrentLanguageUseCase().isArabic()) AppLanguage.ARABIC else AppLanguage.ENGLISH,
        selectedTheme    = if (getCurrentThemeUseCase().isDark()) AppColorTheme.DARK else AppColorTheme.LIGHT,
    )
) {

    override fun onEvent(intent: PreferencesIntent) {
        when (intent) {
            is PreferencesIntent.ChangeLanguage -> {
                val code     = if (intent.language == AppLanguage.ARABIC) "ar" else "en"
                val model    = getLanguages().firstOrNull { it.code == code } ?: ENGLISH_MODEL
                setLanguageUseCase(model)
                setState { copy(selectedLanguage = intent.language) }
            }
            is PreferencesIntent.ChangeTheme -> {
                val model = if (intent.theme == AppColorTheme.DARK) DARK_MODEL else LIGHT_MODEL
                setThemeUseCase(model)
                setState { copy(selectedTheme = intent.theme) }
            }
        }
    }

    fun getLanguages(): List<LanguageModel> = getLanguageUseCase()
    fun getCurrentLanguage(): LanguageModel = getCurrentLanguageUseCase()
    fun getCurrentTheme(): ThemeModel = getCurrentThemeUseCase()
    fun setLanguage(language: LanguageModel) = setLanguageUseCase(language)
    fun setTheme(theme: ThemeModel) = setThemeUseCase(theme)
}