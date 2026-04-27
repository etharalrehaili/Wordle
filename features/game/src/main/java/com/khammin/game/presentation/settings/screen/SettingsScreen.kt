package com.khammin.game.presentation.settings.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    onSignOutClick: Action,
    onLanguageClick: Action = {},
    onSupportClick: Action = {},
    isGuest: Boolean = false,
    uiEffect: SharedFlow<SettingsEffect>,
    onSignOutSuccess: Action,
) {

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                SettingsEffect.SignOutSuccess -> onSignOutSuccess()
                else -> Unit
            }
        }
    }

    SettingsContent(
        onBack          = onBack,
        onSignOutClick  = onSignOutClick,
        onLanguageClick = onLanguageClick,
        onSupportClick  = onSupportClick,
        isGuest         = isGuest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    onBack: Action,
    onSignOutClick: Action,
    onLanguageClick: Action = {},
    onSupportClick: Action = {},
    isGuest: Boolean = false,
) {
    var showSignOutSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // Topbar Section
        GameTopBar(
            title              = stringResource(R.string.settings_title),
            startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
            onStartIconClicked = onBack,
            showBackground     = false,
            modifier           = Modifier.fillMaxWidth().statusBarsPadding(),
            containerColor     = Color.Transparent,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(top = spacing.xs),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // Preferences Section
            SectionLabel(stringResource(R.string.settings_preferences_section))

            Spacer(modifier = Modifier.height(spacing.lg))

            SettingsItem(
                label   = stringResource(R.string.settings_language),
                icon    = Icons.Filled.Language,
                accent  = colors.logoGreen,
                onClick = onLanguageClick,
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            NotificationToggleItem()

            Spacer(modifier = Modifier.height(spacing.sm))

            SettingsItem(
                label   = stringResource(R.string.settings_support),
                icon    = Icons.AutoMirrored.Outlined.HelpOutline,
                accent  = colors.logoPurple,
                onClick = onSupportClick,
            )

            if (!isGuest) {
                Spacer(modifier = Modifier.height(spacing.xl))

                // Account Settings Section
                SectionLabel(stringResource(R.string.settings_account_section))

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
}

@Composable
private fun NotificationToggleItem() {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE) }

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    // Always create the launcher (Compose rule: no conditional composable calls).
    // Only actually used on Android 13+.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        // Mark that we've already shown the system dialog — never show it again.
        prefs.edit().putBoolean("notification_permission_asked", true).apply()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onToggle = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsEnabled) {
            // Android 13+: request the permission the first time; open settings after that.
            val alreadyAsked = prefs.getBoolean("notification_permission_asked", false)
            if (!alreadyAsked) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openNotificationSettings(context)
            }
        } else {
            // Android < 13, or notifications already enabled → open settings.
            openNotificationSettings(context)
        }
    }

    val accent = colors.logoBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.md))
            .background(colors.surface)
            .clickable { onToggle() }
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(spacing.xxl)
                .clip(RoundedCornerShape(spacing.md))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Notifications,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(spacing.md),
            )
        }

        Spacer(modifier = Modifier.width(spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            WordleText(
                text       = stringResource(R.string.settings_notifications),
                color      = colors.title,
                fontSize   = GameDesignTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(spacing.xxs))
            WordleText(
                text     = stringResource(R.string.settings_notifications_subtitle),
                color    = colors.body.copy(alpha = 0.4f),
                fontSize = GameDesignTheme.typography.labelSmall,
            )
        }

        Switch(
            checked         = notificationsEnabled,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = Color.White,
                checkedTrackColor       = accent,
                uncheckedThumbColor     = Color.White,
                uncheckedTrackColor     = colors.border,
                uncheckedBorderColor    = colors.border,
            ),
        )
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
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
                    else          -> colors.surface
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
            onBack         = {},
            onSignOutClick = {},
        )
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewSettingsScreenLight() {
    WordleTheme(appColorTheme = AppColorTheme.LIGHT) {
        SettingsContent(
            onBack         = {},
            onSignOutClick = {},
        )
    }
}
