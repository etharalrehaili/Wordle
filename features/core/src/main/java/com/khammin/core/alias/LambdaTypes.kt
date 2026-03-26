package com.khammin.core.alias

import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage

typealias Action = () -> Unit
typealias IntAction = (Int) -> Unit
typealias ThemeAction = (AppColorTheme) -> Unit
typealias LanguageAction = (AppLanguage) -> Unit