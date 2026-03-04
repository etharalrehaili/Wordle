package com.wordle.core.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val DarkBackground      = Color(0xFF121213)
val DarkSurface         = Color(0xFF1A1A1B)
val DarkTopBar          = Color(0xFF121620)
val DarkDivider         = Color(0xFF3A3D47)
val DarkCorrect         = Color(0xFF538D4E)
val DarkPresent         = Color(0xFFB59F3B)
val DarkAbsent          = Color(0xFF3A3A3C)
val DarkKey             = Color(0xFF818384)
val DarkTitle           = Color(0xFFFFFFFF)
val DarkBody            = Color(0xFFCACBCC)
val DarkBorder       = Color(0xFF3A3A3C)
val DarkBorderActive = Color(0xFF565758)
val DarkActiveTile   = Color(0xFF1565C0)

val LightBackground     = Color(0xFFEEEEF0)
val LightSurface        = Color(0xFFF5F5F5)
val LightTopBar         = Color(0xFFEEEEF0)
val LightDivider        = Color(0xFFD3D6DA)
val LightCorrect        = Color(0xFF6AAA64)
val LightPresent        = Color(0xFFC9B458)
val LightAbsent         = Color(0xFF787C7E)
val LightKey            = Color(0xFFD3D6DA)
val LightTitle          = Color(0xFF1A1A1B)
val LightBody           = Color(0xFF3A3A3C)
val LightBorder       = Color(0xFFD3D6DA)
val LightBorderActive = Color(0xFF999999)
val LightActiveTile   = Color(0xFF1565C0)

data class GameColorScheme(
    // Text Colors
    val textHeading: Color,

    // Surface Colors
    val surfaceShade4: Color,
    val surfaceScreenBg: Color

)

val LocalHabitTrackerColorScheme = compositionLocalOf {
    GameColorScheme(
        textHeading = Color.Unspecified,
        surfaceShade4 = Color.Unspecified,
        surfaceScreenBg = Color.Unspecified
    )
}