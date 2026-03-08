package com.wordle.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.wordle.core.presentation.components.enums.AppColorTheme

@Composable
fun WordleTheme(
    appColorTheme: AppColorTheme = AppColorTheme.DARK,
    content: @Composable () -> Unit
) {
    val wordleColors = when (appColorTheme) {
        AppColorTheme.DARK    -> DarkWordleColors
        AppColorTheme.LIGHT   -> LightWordleColors
    }

    val materialColorScheme = when (appColorTheme) {
        AppColorTheme.LIGHT -> lightColorScheme(
            primary    = LightCorrect,
            background = LightBackground,
            surface    = LightSurface,
        )
        else -> darkColorScheme(
            primary    = wordleColors.correct,
            background = wordleColors.background,
            surface    = wordleColors.surface,
        )
    }

    CompositionLocalProvider(
        LocalWordleColors provides wordleColors,
        LocalHabitTrackerSpacing provides defaultHabitTrackerSpacing(),
        LocalHabitTrackerTypography provides defaultHabitTrackerTypography(),
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography  = Typography,
            content     = content
        )
    }
}

object GameDesignTheme {
    val colors: WordleColors
        @Composable get() = LocalWordleColors.current

    val spacing: HabitTrackerSpacing
        @Composable get() = LocalHabitTrackerSpacing.current

    val typography: HabitTrackerTypography
        @Composable get() = LocalHabitTrackerTypography.current
}

data class WordleColors(
    val background: Color,
    val surface: Color,
    val topBar: Color,
    val divider: Color,
    val correct: Color,
    val present: Color,
    val absent: Color,
    val key: Color,
    val title: Color,
    val body: Color,
    val error: Color,
    val border: Color,
    val borderActive: Color,
    val activeTile: Color,
)

val DarkWordleColors = WordleColors(
    background = DarkBackground,
    surface    = DarkSurface,
    topBar     = DarkTopBar,
    divider    = DarkDivider,
    correct    = DarkCorrect,
    present    = DarkPresent,
    absent     = DarkAbsent,
    key        = DarkKey,
    title      = DarkTitle,
    body       = DarkBody,
    error     = DarkError,
    border       = DarkBorder,
    borderActive = DarkBorderActive,
    activeTile   = DarkActiveTile,
)

val LightWordleColors = WordleColors(
    background = LightBackground,
    surface    = LightSurface,
    topBar     = LightTopBar,
    divider    = LightDivider,
    correct    = LightCorrect,
    present    = LightPresent,
    absent     = LightAbsent,
    key        = LightKey,
    title      = LightTitle,
    body       = LightBody,
    error     = LightError,
    border       = LightBorder,
    borderActive = LightBorderActive,
    activeTile   = LightActiveTile,
)

val LocalWordleColors = compositionLocalOf { DarkWordleColors }
