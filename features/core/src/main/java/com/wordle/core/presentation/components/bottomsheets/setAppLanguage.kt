package com.wordle.core.presentation.components.bottomsheets

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun setAppLanguage(languageCode: String) {
    val localeList = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(localeList)
}