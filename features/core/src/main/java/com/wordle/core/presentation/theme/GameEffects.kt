package com.wordle.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

val LocalHabitTrackerSpacing = staticCompositionLocalOf {
    HabitTrackerSpacing(
        xxs = Dp.Unspecified,
        xs = Dp.Unspecified,
        sm = Dp.Unspecified,
        md = Dp.Unspecified,
        lg = Dp.Unspecified,
        xl = Dp.Unspecified,
        xxl = Dp.Unspecified
    )
}

data class HabitTrackerSpacing(
    val xxs: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp
)