package com.wordle.game.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.wordle.core.presentation.theme.LocalWordleColors
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.GameDesignTheme
import com.wordle.core.presentation.theme.WordleTheme
import com.wordle.game.presentation.settings.contract.SettingsEffect
import kotlinx.coroutines.flow.MutableSharedFlow
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
        // ── Header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.buttonPink.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            GameTopBar(
                title              = "Settings",
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                modifier           = Modifier.fillMaxWidth(),
                containerColor     = Color.Transparent,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Account Settings Section ───────────────────────────────────
            SectionLabel("Account Settings")

            SettingsItem(
                label    = "Change Email",
                icon     = Icons.Filled.Email,
                accent = colors.buttonTeal,
                onClick  = onChangeEmailClick,
            )

            Spacer(Modifier.height(12.dp))

            SettingsItem(
                label    = "Change Password",
                icon     = Icons.Filled.Lock,
                accent = colors.buttonPink,
                onClick  = onChangePasswordClick,
            )



            // ── App Settings Section ───────────────────────────────────
            SectionLabel("App Settings")

            SettingsItem(
                label    = "Notifications",
                icon     = Icons.Filled.Notifications,
                accent   = colors.buttonTaupe,
                onClick  = onChangeEmailClick,
            )

            // ── Support Section ───────────────────────────────────
            SectionLabel("Support")

            SettingsItem(
                label    = "Support",
                icon     = Icons.Filled.QuestionMark,
                accent = colors.buttonTeal,
                onClick  = onChangeEmailClick,
            )

            Spacer(Modifier.height(24.dp))

            // Sign out

            SettingsItem(
                label    = "Sign Out",
                icon     = Icons.AutoMirrored.Filled.ExitToApp,
                accent   = colors.error,
                onClick  = onSignOutClick,
                isDestructive = true,
                showArrow     = false
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = LocalWordleColors.current
    WordleText(
        text          = text,
        color         = colors.body.copy(alpha = 0.4f),
        fontSize      = GameDesignTheme.typography.labelSmall,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier      = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: Action,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    showArrow: Boolean = true
) {

    val colors = LocalWordleColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDestructive) accent.copy(alpha = 0.12f)
                else colors.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = if (isDestructive) 0.25f else 0.15f)),  // ← was always 0.15f
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            WordleText(
                text       = label,
                color      = if (isDestructive) accent else colors.title,
                fontSize   = GameDesignTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                WordleText(
                    text     = subtitle,
                    color    = colors.body.copy(alpha = 0.4f),
                    fontSize = GameDesignTheme.typography.labelSmall,
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = if (isDestructive) accent.copy(alpha = 0.5f)
                else colors.body.copy(alpha = 0.20f),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewSettingsScreenDark() {
    WordleTheme(appColorTheme = AppColorTheme.DARK) {
        SettingsScreen(
            onBack = {},
            onChangeEmailClick = {},
            onChangePasswordClick = {},
            onSignOutClick = {},
            uiEffect = MutableSharedFlow(),
            onSignOutSuccess = {},
        )
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewSettingsScreenLight() {
    WordleTheme(appColorTheme = AppColorTheme.LIGHT) {
        SettingsScreen(
            onBack = {},
            onChangeEmailClick = {},
            onChangePasswordClick = {},
            onSignOutClick = {},
            uiEffect = MutableSharedFlow(),
            onSignOutSuccess = {},
        )
    }
}
