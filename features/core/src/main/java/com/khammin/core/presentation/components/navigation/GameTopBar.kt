package com.khammin.core.presentation.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme.spacing
import java.util.Locale

@Composable
fun GameTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    points: Int? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    hintIcon: ImageVector? = null,
    hintsRemaining: Int = 0,
    onStartIconClicked: Action? = null,
    onEndIconClicked: Action? = null,
    onHintClicked: Action? = null,
    isDarkMode: Boolean = true,
    onThemeToggle: ((AppColorTheme) -> Unit)? = null,
    onCloseClicked: Action? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.xs)
            .clip(RoundedCornerShape(20.dp))
            .height(56.dp),
        contentAlignment = Alignment.Center,
    ) {

        // ── Start slot (left in English) ──────────────────────────────────────────
        // Shows a points badge when `points` is provided, otherwise shows the navigation icon (e.g. back arrow).
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
        ) {
            when {
                points != null -> {
                    // Points badge — pill-shaped chip with the player's score.
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(horizontal = spacing.sm, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        WordleText(
                            text = stringResource(
                                R.string.top_bar_points_label,
                                String.format(Locale.US, "%d", points)
                            ),
                            color         = colors.logoGreen,
                            fontSize      = 13.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }

                startIcon != null && onStartIconClicked != null -> {
                    // Navigation icon
                    IconButton(
                        onClick  = { onStartIconClicked() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector        = startIcon,
                            contentDescription = null,
                            tint               = colors.body,
                            modifier           = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }

        // ── Center slot — screen title ─────────────────────────────────
        if (title != null) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier              = Modifier.align(Alignment.Center),
            ) {
                WordleText(
                    text          = title.uppercase(),
                    color         = colors.title,
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 3.sp,
                )
            }
        }

        // ── End slot (right in English) ───────────────────────────────────────────
        // Renders action buttons in a fixed priority order:
        // theme toggle → hint → close (or generic end icon).
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {

                // Theme toggle — shows sun when dark mode is active (tap → light),
                // and moon when light mode is active (tap → dark).
                if (onThemeToggle != null) {
                    IconButton(
                        onClick = {
                            val newTheme = if (isDarkMode) AppColorTheme.LIGHT else AppColorTheme.DARK
                            onThemeToggle(newTheme)
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector        = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDarkMode)
                                stringResource(R.string.top_bar_switch_to_light)
                            else
                                stringResource(R.string.top_bar_switch_to_dark),
                            tint     = colors.body,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Hint button — disabled and shows an "Ad" badge when no hints remain.
                if (hintIcon != null && onHintClicked != null) {
                    Box {
                        IconButton(
                            onClick  = onHintClicked,
                            enabled  = hintsRemaining > 0,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector        = hintIcon,
                                contentDescription = stringResource(R.string.top_bar_hint_remaining, hintsRemaining),
                                tint     = if (hintsRemaining > 0) colors.body else colors.body.copy(alpha = 0.35f),
                                modifier = Modifier.size(26.dp),
                            )
                        }

                        // Badge overlaid on the top-right corner of the hint button.
                        // Green with a count when hints remain; pink with "Ad" when empty.
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-8).dp)
                                .defaultMinSize(minWidth = 22.dp)
                                .background(
                                    color = if (hintsRemaining > 0) colors.logoGreen else colors.logoPink,
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            WordleText(
                                text       = if (hintsRemaining > 0) "$hintsRemaining"
                                             else stringResource(R.string.top_bar_hint_watch_ad),
                                color      = Color.White,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp,
                            )
                        }
                    }
                }

                // Close button takes priority over the generic end icon.
                // Use `onCloseClicked` for dismiss/cancel actions (e.g. result sheets).
                // Use `endIcon` + `onEndIconClicked` for secondary navigation actions.
                if (onCloseClicked != null) {
                    IconButton(
                        onClick  = { onCloseClicked() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = null,
                            tint               = colors.body,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                } else if (endIcon != null && onEndIconClicked != null) {
                    IconButton(
                        onClick  = { onEndIconClicked() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector        = endIcon,
                            contentDescription = null,
                            tint               = colors.body,
                            modifier           = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewGameTopBarWithBothIcons() {
    GameTopBar(
        title              = "WORDLE",
        startIcon          = Icons.Outlined.Info,
        onStartIconClicked = {},
        endIcon            = Icons.Filled.Menu,
        onEndIconClicked   = {},
    )
}

@GameLightBackgroundPreview
@Composable
private fun PreviewGameTopBarEndIconOnly() {
    GameTopBar(
        endIcon          = Icons.Filled.Menu,
        onEndIconClicked = {},
    )
}
