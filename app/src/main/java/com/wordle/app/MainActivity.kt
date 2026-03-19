package com.wordle.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.wordle.core.presentation.components.enums.AppColorTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.theme.WordleTheme
import com.wordle.core.util.LocaleHelper
import com.wordle.game.presentation.navigation.Route
import com.wordle.game.presentation.navigation.navGraph
import com.wordle.game.presentation.preferences.vm.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // for language and theme
    private val preferenceViewModel: PreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val prefs      = getSharedPreferences("settings", MODE_PRIVATE)
        val savedLang  = prefs.getString("language", Locale.getDefault().language) ?: "en"
        val savedTheme = prefs.getString("theme", "DARK") ?: "DARK"
        LocaleHelper.setLocale(this, savedLang)

        setContent {
            val deviceLanguage = if (Locale.getDefault().language == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
            var appLanguage   by remember { mutableStateOf(deviceLanguage) }
            var appColorTheme by remember { mutableStateOf(AppColorTheme.valueOf(savedTheme)) }

            val isLightTheme = appColorTheme == AppColorTheme.LIGHT
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightTheme
                }
            }

            val layoutDirection = if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            WordleTheme(appColorTheme = appColorTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    val navController = rememberNavController()
                    NavHost(
                        navController    = navController,
                        startDestination = Route.HomeScreen
                    ) {
                        navGraph(
                            navController,
                            onThemeChanged = { theme ->
                                appColorTheme = theme
                                prefs.edit().putString("theme", theme.name).apply()
                            },
                            onLanguageChanged = { language ->
                                val code = if (language == AppLanguage.ARABIC) "ar" else "en"
                                prefs.edit().putString("language", code).apply()
                                LocaleHelper.setLocale(this@MainActivity, code)
                                recreate()
                            },
                            currentLanguage = { appLanguage },
                            currentTheme    = { appColorTheme }
                        )
                    }
                }
            }
        }
    }
}