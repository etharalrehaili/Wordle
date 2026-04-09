package com.khammin.core.presentation.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun GameTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    hintIcon: ImageVector? = null,
    hintsRemaining: Int = 0,
    onStartIconClicked: Action? = null,
    onEndIconClicked: Action? = null,
    onHintClicked: Action? = null,
    containerColor: Color? = null,
    isDarkMode: Boolean = true,
    onThemeToggle: ((AppColorTheme) -> Unit)? = null,
    showBackground: Boolean = true,
    onCloseClicked: Action? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (showBackground)
                    Modifier
                        .background(color = colors.topBarBackground)
                        .border(
                            width = 1.dp,
                            color = colors.topBarBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                else Modifier
            )
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Start icon
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
        ) {
            if (startIcon != null && onStartIconClicked != null) {
                IconButton(
                    onClick = { onStartIconClicked() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = startIcon,
                        contentDescription = null,
                        tint = colors.body,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // title
        if (title != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.Center)
            ) {
                WordleText(
                    text = title.uppercase(),
                    color = colors.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 3.sp,
                )
            }
        }

        // End icon / hint icon
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Theme toggle — sun / moon
                if (onThemeToggle != null) {
                    IconButton(
                        onClick = {
                            val newTheme = if (isDarkMode) AppColorTheme.LIGHT else AppColorTheme.DARK
                            onThemeToggle?.invoke(newTheme)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode)
                                Icons.Outlined.LightMode
                            else
                                Icons.Outlined.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to light mode" else "Switch to dark mode",
                            tint = colors.body,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (hintIcon != null && onHintClicked != null) {
                    Box {
                        IconButton(
                            onClick = onHintClicked,
                            enabled = hintsRemaining > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = hintIcon,
                                contentDescription = "Use hint ($hintsRemaining remaining)",
                                tint = if (hintsRemaining > 0) colors.body
                                else colors.body.copy(alpha = 0.35f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-8).dp)
                                .defaultMinSize(minWidth = 22.dp)
                                .background(
                                    color = if (hintsRemaining > 0) colors.buttonTeal else colors.pinkText,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (hintsRemaining > 0) "$hintsRemaining" else "Ad",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
                if (onCloseClicked != null) {
                    IconButton(
                        onClick  = { onCloseClicked() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = null,
                            tint               = colors.body,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                } else if (endIcon != null && onEndIconClicked != null) {
                    IconButton(
                        onClick = { onEndIconClicked() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = endIcon,
                            contentDescription = null,
                            tint = colors.body,
                            modifier = Modifier.size(24.dp)
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
        title = "WORDLE",
        startIcon = Icons.Outlined.Info,
        onStartIconClicked = {},
        endIcon = Icons.Filled.Menu,
        onEndIconClicked = {}
    )
}

@GameLightBackgroundPreview
@Composable
private fun PreviewGameTopBarEndIconOnly() {
    GameTopBar(
        endIcon = Icons.Filled.Menu,
        onEndIconClicked = {}
    )
}