package com.wordle.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.presentation.contract.SettingsEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SettingsScreen(
    onBack: Action,
    onChangeEmailClick: Action,
    onChangePasswordClick: Action,
    onSignOutClick: Action,
    uiEffect: SharedFlow<SettingsEffect>,
    onSignOutSuccess: Action,
) {
    val colors = LocalWordleColors.current

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                SettingsEffect.SignOutSuccess -> onSignOutSuccess()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        GameTopBar(
            title              = "Settings",
            startIcon          = Icons.Filled.ArrowBack,
            onStartIconClicked = onBack,
            modifier           = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text          = "Account",
                color         = colors.title,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                label    = "Change Email",
                icon     = Icons.Filled.Email,
                onClick  = onChangeEmailClick,
            )

            Spacer(Modifier.height(12.dp))

            SettingsItem(
                label    = "Change Password",
                icon     = Icons.Filled.Lock,
                onClick  = onChangePasswordClick,
            )

            Spacer(Modifier.height(12.dp))

            SettingsItem(
                label      = "Sign Out",
                icon       = Icons.AutoMirrored.Filled.ExitToApp,
                onClick    = onSignOutClick,
                tintColor  = colors.error,
                labelColor = colors.error,
            )
        }
    }
}

@Composable
private fun SettingsItem(
    label: String,
    icon: ImageVector,
    onClick: Action,
    tintColor: androidx.compose.ui.graphics.Color = LocalWordleColors.current.body,
    labelColor: androidx.compose.ui.graphics.Color = LocalWordleColors.current.title,
    ) {
    val colors = LocalWordleColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tintColor,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text       = label,
            color      = labelColor,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f)
        )
        Icon(
            imageVector        = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint               = tintColor,
            modifier           = Modifier.size(20.dp)
        )
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewSettingsScreenDark() {
    SettingsScreen(
        onBack                = {},
        onChangeEmailClick    = {},
        onChangePasswordClick = {},
        onSignOutClick        = {},
        uiEffect              = MutableSharedFlow(),
        onSignOutSuccess      = {},
    )
}
