package com.wordle.core.alias

import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage

typealias Action = () -> Unit
typealias IntAction = (Int) -> Unit
typealias ThemeAction = (AppColorTheme) -> Unit
typealias LanguageAction = (AppLanguage) -> Unit