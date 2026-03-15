package com.wordle.core.presentation.components.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    onStartIconClicked: Action? = null,
    onEndIconClicked: Action? = null,
    containerColor: Color? = null,
) {
    val colors = LocalWordleColors.current
    val appBarColor = containerColor ?: colors.background

    TopAppBar(
        title = {
            if (title != null) {
                WordleText(
                    text = title,
                    color = colors.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 3.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = {
            if (startIcon != null && onStartIconClicked != null) {
                IconButton(
                    onClick = onStartIconClicked,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = startIcon,
                        contentDescription = null,
                        tint = colors.body,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        actions = {
            Spacer(modifier = Modifier.size(48.dp))

            if (endIcon != null && onEndIconClicked != null) {
                IconButton(
                    onClick = onEndIconClicked,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = endIcon,
                        contentDescription = null,
                        tint = colors.body,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = appBarColor,
            scrolledContainerColor     = appBarColor,
            titleContentColor          = colors.title,
            navigationIconContentColor = colors.body,
            actionIconContentColor     = colors.body
        ),
        modifier = modifier
    )

    HorizontalDivider(color = colors.divider, thickness = 1.dp)
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