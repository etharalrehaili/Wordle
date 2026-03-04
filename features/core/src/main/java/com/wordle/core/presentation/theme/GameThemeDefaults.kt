package com.wordle.core.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Default Color Scheme
fun defaultHabitTrackerColorScheme() = GameColorScheme(
    textHeading = Color(0xFF000000),
    surfaceShade4 = Color(0xFFFFFFFF),
    surfaceScreenBg = Color(0xFFf9fafb)
)

// Default Spacing
fun defaultHabitTrackerSpacing() = HabitTrackerSpacing(
    xxs = 4.dp,
    xs = 8.dp,
    sm = 12.dp,
    md = 16.dp,
    lg = 24.dp,
    xl = 32.dp,
    xxl = 48.dp
)