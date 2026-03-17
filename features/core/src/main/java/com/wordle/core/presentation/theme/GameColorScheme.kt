package com.wordle.core.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val DarkBackground      = Color(0xFF2c313d)
val DarkSurface         = Color(0xFF1A1A1B)
val DarkTopBar          = Color(0xFF121620)
val DarkDivider         = Color(0xFF3A3D47)
val DarkCorrect         = Color(0xFF7BC47F)
val DarkPresent         = Color(0xFFE0C96A)
val DarkAbsent          = Color(0xFF3A3A3C)
val DarkKey             = Color(0xFF818384)
val DarkTitle           = Color(0xFFFFFFFF)
val DarkBody            = Color(0xFFCACBCC)
val DarkError  = Color(0xFFCF6679)
val DarkBorder       = Color(0xFF3A3A3C)
val DarkBorderActive = Color(0xFF565758)
val DarkActiveTile   = Color(0xFF1565C0)
val DarkSoftPink = Color(0xFFe7a1c8)
val DarkCountdownBackground = Color(0xFF3A4050)
val DarkButtonPink    = Color(0xFFC272A4)
val DarkButtonTeal    = Color(0xFF6aafbf)
val DarkButtonTaupe   = Color(0xFF7a7370)

val LightBackground     = Color(0xFFF2F0EF)
val LightSurface        = Color(0xFFF5F5F5)
val LightTopBar         = Color(0xFFEEEEF0)
val LightDivider        = Color(0xFFD3D6DA)
val LightCorrect        = Color(0xFF8ED192)
val LightPresent        = Color(0xFFE8D98A)
val LightAbsent         = Color(0xFF787C7E)
val LightKey            = Color(0xFFD3D6DA)
val LightTitle          = Color(0xFF1A1A1B)
val LightBody           = Color(0xFF3A3A3C)
val LightError = Color(0xFFB85263)
val LightBorder       = Color(0xFFD3D6DA)
val LightBorderActive = Color(0xFF999999)
val LightActiveTile   = Color(0xFF1565C0)
val LightRosePink = Color(0xFFC85A8E)
val LightCountdownBackground = Color(0xFFE3E6EC)
val LightButtonPink   = Color(0xFFe7a1c8)
val LightButtonTeal   = Color(0xFF92d0dc)
val LightButtonTaupe  = Color(0xFFa59c97)

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