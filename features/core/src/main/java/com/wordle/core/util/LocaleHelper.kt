package com.wordle.core.util

import android.app.Activity
import java.util.Locale

object LocaleHelper {
    fun setLocale(activity: Activity, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = activity.resources.configuration
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
}