package com.wordle.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val LocalHabitTrackerTypography = staticCompositionLocalOf {
    HabitTrackerTypography(
        displayLarge = TextUnit.Unspecified,
        displayMedium = TextUnit.Unspecified,
        displaySmall = TextUnit.Unspecified,
        headingLarge = TextUnit.Unspecified,
        headingMedium = TextUnit.Unspecified,
        headingSmall = TextUnit.Unspecified,
        titleLarge = TextUnit.Unspecified,
        titleMedium = TextUnit.Unspecified,
        titleSmall = TextUnit.Unspecified,
        bodyLarge = TextUnit.Unspecified,
        bodyMedium = TextUnit.Unspecified,
        bodySmall = TextUnit.Unspecified,
        labelLarge = TextUnit.Unspecified,
        labelMedium = TextUnit.Unspecified,
        labelSmall = TextUnit.Unspecified,
        captionLarge = TextUnit.Unspecified,
        captionSmall = TextUnit.Unspecified
    )
}

data class HabitTrackerTypography(
    // Display - Largest text (48sp+)
    val displayLarge: TextUnit,    // 48sp - "Today" heading
    val displayMedium: TextUnit,   // 40sp
    val displaySmall: TextUnit,    // 36sp

    // Heading - Section titles (28-32sp)
    val headingLarge: TextUnit,    // 32sp - "Daily Habits", "No habits yet"
    val headingMedium: TextUnit,   // 28sp
    val headingSmall: TextUnit,    // 24sp

    // Title - Component titles (18-22sp)
    val titleLarge: TextUnit,      // 22sp
    val titleMedium: TextUnit,     // 18sp - Buttons, Tab Bar items, TextField input
    val titleSmall: TextUnit,      // 16sp

    // Body - Regular text (14-16sp)
    val bodyLarge: TextUnit,       // 16sp - Description text, Labels
    val bodyMedium: TextUnit,      // 15sp
    val bodySmall: TextUnit,       // 14sp - Day names (WED, THU), Date text

    // Label - Small text (12-13sp)
    val labelLarge: TextUnit,      // 13sp
    val labelMedium: TextUnit,     // 12sp - Bottom tab labels
    val labelSmall: TextUnit,      // 11sp

    // Caption - Tiny text (10-11sp)
    val captionLarge: TextUnit,    // 11sp
    val captionSmall: TextUnit     // 10sp
)

// Default implementation with actual values
fun defaultHabitTrackerTypography() = HabitTrackerTypography(
    displayLarge = 48.sp,
    displayMedium = 40.sp,
    displaySmall = 36.sp,

    headingLarge = 32.sp,
    headingMedium = 28.sp,
    headingSmall = 24.sp,

    titleLarge = 22.sp,
    titleMedium = 18.sp,
    titleSmall = 16.sp,

    bodyLarge = 16.sp,
    bodyMedium = 15.sp,
    bodySmall = 14.sp,

    labelLarge = 13.sp,
    labelMedium = 12.sp,
    labelSmall = 11.sp,

    captionLarge = 11.sp,
    captionSmall = 10.sp
)