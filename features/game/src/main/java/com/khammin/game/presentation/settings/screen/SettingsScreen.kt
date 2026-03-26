package com.khammin.game.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.khammin.core.presentation.theme.LocalWordleColors
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.bottomsheets.SignOutConfirmationBottomSheet
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme.spacing
import com.khammin.core.presentation.theme.WordleTheme
import com.khammin.game.R
import com.khammin.game.presentation.settings.contract.SettingsEffect
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: Action,
    onChangePasswordClick: Action,
    onSignOutClick: Action,
    onSupportClick: Action,
    uiEffect: SharedFlow<SettingsEffect>,
    onSignOutSuccess: Action,
) {

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                SettingsEffect.SignOutSuccess            -> onSignOutSuccess()
                SettingsEffect.NavigateToChangePassword  -> onChangePasswordClick()
                else -> Unit
            }
        }
    }

    SettingsContent(
        onBack                = onBack,
        onChangePasswordClick = onChangePasswordClick,
        onSignOutClick        = onSignOutClick,
        onSupportClick = onSupportClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    onBack: Action,
    onChangePasswordClick: Action,
    onSupportClick: Action,
    onSignOutClick: Action,
) {
    var showSignOutSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // Topbar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GameTopBar(
                title              = stringResource(R.string.settings_title),
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                modifier           = Modifier.fillMaxWidth(),
                containerColor     = Color.Transparent,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(top = spacing.xs),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // Account Settings Section
            SectionLabel(stringResource(R.string.settings_account_section))

            SettingsItem(
                label   = stringResource(R.string.settings_change_password),
                icon    = Icons.Filled.Lock,
                accent  = colors.buttonTaupe,
                onClick = onChangePasswordClick,
            )

            // App Settings Section
            SectionLabel(stringResource(R.string.settings_app_section))

            SettingsItem(
                label   = stringResource(R.string.settings_notifications),
                icon    = Icons.Filled.Notifications,
                accent  = colors.buttonTaupe,
                onClick = {},
            )

            // Support Section
            SectionLabel(stringResource(R.string.settings_support_section))

            SettingsItem(
                label   = stringResource(R.string.settings_support),
                icon    = Icons.Filled.QuestionMark,
                accent  = colors.buttonTaupe,
                onClick = onSupportClick
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            // Sign Out
            SettingsItem(
                label         = stringResource(R.string.sign_out_title),
                icon          = Icons.AutoMirrored.Filled.ExitToApp,
                accent        = colors.error,
                onClick       = { showSignOutSheet = true },
                isDestructive = true,
                showArrow     = false,
            )

            if (showSignOutSheet) {
                SignOutConfirmationBottomSheet(
                    onDismiss = { showSignOutSheet = false },
                    onConfirm = {
                        showSignOutSheet = false
                        onSignOutClick()
                    }
                )
            }
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
        modifier      = Modifier.padding(horizontal = spacing.xxs, vertical = spacing.sm)
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
    showArrow: Boolean = true,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.md))
            .background(
                when {
                    isDestructive -> accent.copy(alpha = 0.12f)
                    else          -> colors.buttonTaupe.copy(alpha = 0.10f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(spacing.xxl)
                .clip(RoundedCornerShape(spacing.md))
                .background(accent.copy(alpha = if (isDestructive) 0.25f else 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(spacing.md)
            )
        }

        Spacer(modifier = Modifier.width(spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            WordleText(
                text       = label,
                color      = if (isDestructive) accent else colors.title,
                fontSize   = GameDesignTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(spacing.xxs))
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
                modifier           = Modifier.size(spacing.md)
            )
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewSettingsScreenDark() {
    WordleTheme(appColorTheme = AppColorTheme.DARK) {
        SettingsContent(
            onBack                = {},
            onChangePasswordClick = {},
            onSignOutClick        = {},
            onSupportClick = {}
        )
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewSettingsScreenLight() {
    WordleTheme(appColorTheme = AppColorTheme.LIGHT) {
        SettingsContent(
            onBack                = {},
            onChangePasswordClick = {},
            onSignOutClick        = {},
            onSupportClick = {}
        )
    }
}