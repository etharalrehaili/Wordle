package com.khammin.app

import android.R
import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.khammin.core.domain.model.DARK_MODEL
import com.khammin.core.domain.model.ENGLISH_MODEL
import com.khammin.core.domain.model.LIGHT_MODEL
import com.khammin.core.domain.model.Languages
import com.khammin.core.domain.model.isArabic
import com.khammin.core.domain.model.isDark
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.theme.WordleTheme
import com.khammin.core.util.LocaleHelper
import com.khammin.game.presentation.navigation.Route
import com.khammin.game.presentation.navigation.navGraph
import com.khammin.game.presentation.preferences.vm.PreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val preferenceViewModel: PreferencesViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setBackgroundDrawableResource(R.color.transparent)

        handleInitialSelectedLanguage()
        handleInitialSelectedTheme()

        setContent {
            val currentLang = preferenceViewModel.getCurrentLanguage()
            val currentTheme = preferenceViewModel.getCurrentTheme()
            var appLanguage by remember { mutableStateOf(if (currentLang.isArabic()) AppLanguage.ARABIC else AppLanguage.ENGLISH) }
            var appColorTheme by remember { mutableStateOf(if (currentTheme.isDark()) AppColorTheme.DARK else AppColorTheme.LIGHT) }

            val isLightTheme = appColorTheme == AppColorTheme.LIGHT
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        isLightTheme
                }
            }

            val layoutDirection =
                if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            WordleTheme(appColorTheme = appColorTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Route.HomeScreen
                        ) {
                            navGraph(
                                navController,
                                onThemeChanged = { theme ->
                                    appColorTheme = theme
                                    val themeModel =
                                        if (theme == AppColorTheme.DARK) DARK_MODEL else LIGHT_MODEL
                                    preferenceViewModel.setTheme(themeModel)
                                },
                                onLanguageChanged = { language ->
                                    val code = if (language == AppLanguage.ARABIC) "ar" else "en"
                                    val selected = preferenceViewModel.getLanguages()
                                        .firstOrNull { it.code == code } ?: ENGLISH_MODEL
                                    preferenceViewModel.setLanguage(selected)
                                    LocaleHelper.setLocale(this@MainActivity, code)
                                    recreate()
                                },
                                currentLanguage = { appLanguage },
                                currentTheme = { appColorTheme }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleInitialSelectedLanguage() {
        val savedCode = preferenceViewModel.getCurrentLanguage().code

        val resolvedCode = savedCode.ifEmpty {
            val deviceLang = Locale.getDefault().language
            if (deviceLang == Languages.AR.code || deviceLang == Languages.EN.code) {
                deviceLang
            } else {
                Languages.EN.code
            }
        }

        val selectedLanguage = preferenceViewModel.getLanguages()
            .firstOrNull { it.code == resolvedCode } ?: ENGLISH_MODEL
        preferenceViewModel.setLanguage(selectedLanguage)
        LocaleHelper.setLocale(this, resolvedCode)
    }

    private fun handleInitialSelectedTheme() {
        val savedTheme = preferenceViewModel.getCurrentTheme()

        // Check if it's the serializer default (id = 1 = DARK_MODEL)
        // On first launch DataStore returns defaultValue which is DARK_MODEL
        // We need to detect first launch differently
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isFirstLaunch = !prefs.getBoolean("theme_initialized", false)

        val resolvedTheme = if (isFirstLaunch) {
            // Use device theme
            val isDeviceDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            val theme = if (isDeviceDark) DARK_MODEL else LIGHT_MODEL
            prefs.edit().putBoolean("theme_initialized", true).apply()
            theme
        } else {
            // Use saved theme
            savedTheme
        }

        preferenceViewModel.setTheme(resolvedTheme)
    }

}
