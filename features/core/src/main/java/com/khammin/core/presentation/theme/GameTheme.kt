package com.khammin.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.khammin.core.presentation.components.enums.AppColorTheme

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
    val pinkText: Color,
    val countdownBackground: Color,
    val buttonPink: Color,
    val buttonTeal: Color,
    val buttonTaupe: Color,
    val decorativeTeal: Color,
    val decorativeGreen: Color,
    val decorativePink: Color,
    val purpleButton: Color,
    val gradientPrimary: Color,
    val gradientSecondary: Color,
    val gradientAccent: Color,
    val gradientBackground: Color,
    val topBarBackground: Color,
    val topBarBorder: Color,
    val buttonPinkBrush: Brush,
    val buttonTealBrush: Brush,
    val buttonTaupeBrush: Brush,
    val cardBackground: Color,
    val cardBorder: Color,
    val buttonPrimaryBg: Color,
    val buttonPrimaryContent: Color,
    val buttonMutedBg: Color,
    val buttonMutedContent: Color,
    val buttonGhostBorder: Color,
    val buttonGhostContent: Color,
    val logoStripBrush: Brush,
    val logoBlue: Color,
    val logoGreen: Color,
    val logoPink: Color,
    val tileDefault: Color,
    val emptyTile: Color,
    val snackbarSuccess: Color,
    val snackbarError: Color,
    val snackbarWarning: Color,
    val logoOrange: Color,
    val logoTeal: Color,
    val logoPurple: Color,
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
    pinkText = DarkSoftPink,
    countdownBackground = DarkCountdownBackground,
    buttonPink = DarkButtonPink,
    buttonTeal = DarkButtonTeal,
    buttonTaupe = DarkButtonTaupe,
    decorativeTeal  = DecorativeTeal,
    decorativeGreen = DecorativeGreen,
    decorativePink  = DecorativePink,
    purpleButton = DarkButtonPurple,
    gradientPrimary    = Color(0xFF8B7CF6).copy(alpha = 0.20f),
    gradientSecondary  = Color(0xFFEC4899).copy(alpha = 0.15f),
    gradientAccent     = Color(0xFF6366F1).copy(alpha = 0.18f),
    gradientBackground = DarkBackground,
    topBarBackground = DarkTopBarBackground,
    topBarBorder     = DarkTopBarBorder,
    buttonPinkBrush  = DarkButtonPinkBrush,
    buttonTealBrush  = DarkButtonTealBrush,
    buttonTaupeBrush = DarkButtonPinkBottomBrush,
    cardBackground = DarkCardBackground,
    cardBorder     = DarkCardBorder,
    buttonPrimaryBg      = ButtonPrimaryBg,
    buttonPrimaryContent = ButtonPrimaryContent,
    buttonMutedBg        = ButtonMutedBgDark,
    buttonMutedContent   = ButtonMutedContentDark,
    buttonGhostBorder    = ButtonGhostBorder,
    buttonGhostContent   = ButtonGhostContentDark,
    logoStripBrush = Brush.horizontalGradient(
        colors = listOf(LogoBlue, LogoPink, LogoGreen)
    ),
    logoBlue  = LogoBlue,
    logoGreen = LogoGreen,
    logoPink  = LogoPink,
    tileDefault = TileDefault,
    emptyTile = DarkEmptyTile,
    snackbarSuccess = LogoGreen,
    snackbarError   = LogoPink,
    snackbarWarning = LogoOrange,
    logoOrange = LogoOrange,
    logoTeal   = LogoTeal,
    logoPurple = LogoPurple,
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
    pinkText = LightRosePink,
    countdownBackground = LightCountdownBackground,
    buttonPink = LightButtonPink,
    buttonTeal = LightButtonTeal,
    buttonTaupe = LightButtonTaupe,
    decorativeTeal  = DecorativeTeal,
    decorativeGreen = DecorativeGreen,
    decorativePink  = DecorativePink,
    purpleButton = LightButtonPurple,
    gradientPrimary    = Color(0xFF8B7CF6).copy(alpha = 0.45f),  // purple
    gradientSecondary  = Color(0xFFEC9FB3).copy(alpha = 0.40f),  // pink
    gradientAccent     = Color(0xFF9BB5F0).copy(alpha = 0.35f),  // blue-purple
    gradientBackground = LightBackground,
    topBarBackground = LightTopBarBackground,
    topBarBorder     = LightTopBarBorder,
    buttonPinkBrush  = LightButtonPinkBrush,
    buttonTealBrush  = LightButtonTealBrush,
    buttonTaupeBrush = LightButtonPinkBottomBrush,
    cardBackground = LightCardBackground,
    cardBorder     = LightCardBorder,
    buttonPrimaryBg      = ButtonPrimaryBg,
    buttonPrimaryContent = ButtonPrimaryContent,
    buttonMutedBg        = ButtonMutedBgLight,
    buttonMutedContent   = ButtonMutedContentLight,
    buttonGhostBorder    = ButtonGhostBorder,
    buttonGhostContent   = ButtonGhostContentLight,
    logoStripBrush = Brush.horizontalGradient(
        colors = listOf(LogoBlue, LogoPink, LogoGreen)
    ),
    logoBlue  = LogoBlue,
    logoGreen = LogoGreen,
    logoPink  = LogoPink,
    tileDefault = TileDefault,
    emptyTile = LightEmptyTile,
    snackbarSuccess = LogoGreen,
    snackbarError   = LogoPink,
    snackbarWarning = LogoOrange,
    logoOrange = LogoOrange,
    logoTeal   = LogoTeal,
    logoPurple = LogoPurple,
)

val LocalWordleColors = compositionLocalOf { DarkWordleColors }
