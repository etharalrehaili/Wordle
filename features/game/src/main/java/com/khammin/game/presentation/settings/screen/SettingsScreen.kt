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

// SharedPreferences file name and key used by the notification toggle.
// Kept as constants to avoid typo-prone string duplication across recompositions.
private const val PREFS_APP_SETTINGS             = "app_settings_prefs"
private const val PREFS_KEY_NOTIFICATION_ASKED   = "notification_permission_asked"

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

        // Navigation bar — back arrow + screen title.
        GameTopBar(
            title              = stringResource(R.string.settings_title),
            startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
            onStartIconClicked = onBack,
            modifier           = Modifier.fillMaxWidth().statusBarsPadding(),
        )

        // Scrollable settings list — items are separated by explicit Spacers.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(top = spacing.xs),
        ) {

            // ── Preferences section ────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_preferences_section))

            // Language — opens the in-app language picker.
            SettingsItem(
                label   = stringResource(R.string.settings_language),
                icon    = Icons.Filled.Language,
                accent  = colors.logoGreen,
                onClick = onLanguageClick,
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            // Notifications — shows the system permission dialog on Android 13+,
            // or opens the app's notification settings for older versions.
            NotificationToggleItem()

            Spacer(modifier = Modifier.height(spacing.sm))

            // ── Support section ────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_support_section))

            // Support — navigates to the Support screen.
            SettingsItem(
                label   = stringResource(R.string.settings_support),
                icon    = Icons.AutoMirrored.Outlined.HelpOutline,
                accent  = colors.logoPurple,
                onClick = onSupportClick,
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            // ── Account Settings section (signed-in users only) ────────
            if (!isGuest) {

                SectionLabel(stringResource(R.string.settings_account_section))

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
    val prefs   = remember { context.getSharedPreferences(PREFS_APP_SETTINGS, Context.MODE_PRIVATE) }

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    // Always create the launcher (Compose rule: no conditional composable calls).
    // Only actually invoked on Android 13+.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        // Mark that we've already shown the system dialog — never show it again.
        prefs.edit().putBoolean(PREFS_KEY_NOTIFICATION_ASKED, true).apply()
    }

    // Re-read the real permission state every time the screen resumes (the user
    // may have toggled it from the system settings while the app was backgrounded).
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
            val alreadyAsked = prefs.getBoolean(PREFS_KEY_NOTIFICATION_ASKED, false)
            if (!alreadyAsked) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openNotificationSettings(context)
            }
        } else {
            // Android < 13, or notifications already enabled → open system settings.
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
        // Icon badge.
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

        // Label and subtitle.
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

        // Toggle — reflects the real system permission state.
        Switch(
            checked         = notificationsEnabled,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor    = Color.White,
                checkedTrackColor    = accent,
                uncheckedThumbColor  = Color.White,
                uncheckedTrackColor  = colors.border,
                uncheckedBorderColor = colors.border,
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

/**
 * A single tappable settings row consisting of a coloured icon badge, a label
 * (and optional subtitle), and an optional trailing chevron.
 *
 * @param label         Primary text shown in the row.
 * @param icon          Icon displayed inside the badge on the left.
 * @param accent        Brand colour applied to the badge background and icon tint.
 * @param onClick       Called when the row is tapped.
 * @param subtitle      Optional secondary line shown below [label].
 * @param isDestructive When true, the row uses a red-tinted background and
 *                      [accent] as the label colour to signal a dangerous action.
 * @param showArrow     Whether to show a trailing chevron. Pass `false` for
 *                      actions that don't navigate (e.g. sign-out).
 */
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
                if (isDestructive) accent.copy(alpha = 0.12f) else colors.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coloured icon badge.
        Box(
            modifier = Modifier
                .size(spacing.xxl)
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

        // Label (and optional subtitle).
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

        // Trailing chevron — omitted for destructive actions like sign-out.
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
