package com.wordle.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.wordle.game.presentation.navigation.Route
import com.wordle.game.presentation.navigation.navGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var appColorTheme by remember { mutableStateOf(AppColorTheme.DARK) }
            var appLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

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
                        navController = navController,
                        startDestination = Route.OnboardingScreen
                    ) {
                        navGraph(
                            navController,
                            onThemeChanged = { appColorTheme = it },
                            onLanguageChanged = { appLanguage = it },
                            currentLanguage = { appLanguage }
                        )
                    }
                }
            }
        }
    }
}