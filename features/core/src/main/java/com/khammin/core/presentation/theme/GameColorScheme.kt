package com.khammin.core.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
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
val DarkButtonPurple = Color(0xFF7B5EA7)

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
val LightButtonPurple  = Color(0xFF9B7BC7)
val DecorativeTeal  = Color(0xFF4DD0E1)
val DecorativeGreen = Color(0xFF4EC9A0)
val DecorativePink  = Color(0xFFF06292)
val LightTopBarBackground = Color(0xFFFFFFFF).copy(alpha = 0.55f)
val LightTopBarBorder     = Color(0xFFFFFFFF).copy(alpha = 0.60f)
val DarkTopBarBackground  = Color(0xFF1A1A2E).copy(alpha = 0.55f)
val DarkTopBarBorder      = Color(0xFFFFFFFF).copy(alpha = 0.15f)

// Dark mode brushes
val DarkButtonPinkBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF5144a3), Color(0xFF654fcc))
)
val DarkButtonTealBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF3b7a9e), Color(0xFF4597c5))
)
val DarkButtonPinkBottomBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF984882), Color(0xFFc2579f))
)

val LightButtonPinkBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF917EE5), Color(0xFFADA0EF))
)
val LightButtonTealBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF76B9E3), Color(0xFF9DCCE8))
)
val LightButtonPinkBottomBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFE182C2), Color(0xFFECA7D5))
)

val LightCardBackground = Color(0xFFFFFFFF).copy(alpha = 0.35f)
val LightCardBorder     = Color(0xFFFFFFFF).copy(alpha = 0.60f)

val DarkCardBackground  = Color(0xFF2A2D3E).copy(alpha = 0.55f)
val DarkCardBorder      = Color(0xFFFFFFFF).copy(alpha = 0.10f)

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