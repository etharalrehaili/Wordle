package com.khammin.game.presentation.profile.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.PlayerAvatar
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme.spacing
import com.khammin.core.presentation.theme.GameDesignTheme.typography
import com.khammin.game.R
import com.khammin.game.presentation.profile.contract.ProfileEffect
import com.khammin.game.presentation.profile.contract.ProfileUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    uiEffect: SharedFlow<ProfileEffect>,
    onBack: Action,
    onSettingsClick: Action,
    onEditProfileClick: Action,
    onSaveProfileClick: Action,
    onCancelEditClick: Action,
    onNameChanged: (String) -> Unit,
    onAvatarChanged: (Uri?) -> Unit,
) {

    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val profileSavedMessage = stringResource(R.string.profile_updated_successfully)

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowError ->
                    snackbarState = SnackbarState(effect.message, SnackbarType.ERROR)
                ProfileEffect.ProfileSaved ->
                    snackbarState = SnackbarState(profileSavedMessage, SnackbarType.SUCCESS)
            }
        }
    }

    ProfileContent(
        uiState            = uiState,
        onBack             = onBack,
        onSettingsClick    = onSettingsClick,
        onEditProfileClick = onEditProfileClick,
        onSaveProfileClick = onSaveProfileClick,
        onCancelEditClick  = onCancelEditClick,
        onNameChanged      = onNameChanged,
        onAvatarChanged    = onAvatarChanged,
    )

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = { snackbarState = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onBack: Action,
    onSettingsClick: Action,
    onEditProfileClick: Action,
    onSaveProfileClick: Action,
    onCancelEditClick: Action,
    onNameChanged: (String) -> Unit,
    onAvatarChanged: (Uri?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Topbar Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                GameTopBar(
                    startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                    onStartIconClicked = onBack,
                    endIcon            = Icons.Filled.Settings,
                    onEndIconClicked   = onSettingsClick,
                    containerColor     = Color.Transparent,
                    modifier           = Modifier.fillMaxWidth(),
                )
            }

            // Avatar + Name Centered
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacing.md))

                if (uiState.isEditMode) {
                    EditProfileSection(
                        editName         = uiState.editName,
                        avatarUrl        = uiState.avatarUrl,
                        pendingAvatarUri = uiState.pendingAvatarUri,
                        onNameChanged    = onNameChanged,
                        onAvatarChanged  = onAvatarChanged,
                        onSave           = onSaveProfileClick,
                        onCancel         = onCancelEditClick,
                    )
                } else {
                    // Avatar
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(spacing.avatarMd)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(colors.buttonPink, colors.buttonTeal)
                                    ),
                                    shape = CircleShape
                                )
                                .padding(spacing.xxs)
                                .clip(CircleShape)
                        ) {
                            PlayerAvatar(
                                name      = uiState.name,
                                avatarUrl = uiState.avatarUrl,
                                modifier  = Modifier.fillMaxSize(),
                                fontSize  = typography.headingMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.sm))

                    WordleText(
                        text       = uiState.name,
                        color      = colors.title,
                        fontSize   = typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    Spacer(modifier = Modifier.height(spacing.xxs))

                    WordleText(
                        text     = uiState.email,
                        color    = colors.body.copy(alpha = 0.5f),
                        fontSize = typography.labelSmall,
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    // Edit button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.roundFull))
                            .background(colors.buttonPink.copy(alpha = 0.12f))
                            .border(
                                1.dp,
                                colors.buttonPink.copy(alpha = 0.35f),
                                RoundedCornerShape(spacing.roundFull)
                            )
                            .clickable { onEditProfileClick() }
                            .padding(horizontal = spacing.md, vertical = spacing.xxs + 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Edit,
                                contentDescription = null,
                                tint               = colors.buttonPink,
                                modifier           = Modifier.size(spacing.sm)
                            )
                            WordleText(
                                text       = stringResource(R.string.profile_edit_button),
                                color      = colors.buttonPink,
                                fontSize   = typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.md))

                }
            }

            // Stats Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
                    .padding(top = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.md))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colors.buttonPink.copy(alpha = 0.20f),
                                    colors.buttonTeal.copy(alpha = 0.20f),
                                )
                            )
                        )
                        .padding(horizontal = spacing.md, vertical = spacing.md)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            WordleText(
                                text          = stringResource(R.string.profile_total_points),
                                color         = colors.body.copy(alpha = 0.55f),
                                fontSize      = typography.labelSmall,
                                fontWeight    = FontWeight.Medium,
                                letterSpacing = 0.5.sp,
                            )
                            Spacer(modifier = Modifier.height(spacing.xxs))
                            WordleText(
                                text       = "%,d".format(uiState.enCurrentPoints + uiState.arCurrentPoints),
                                color      = colors.title,
                                fontSize   = typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(spacing.avatarSm)
                                .clip(CircleShape)
                                .background(colors.buttonPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Star,
                                contentDescription = null,
                                tint               = colors.buttonPink,
                                modifier           = Modifier.size(spacing.lg)
                            )
                        }
                    }
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    // Stats cards — sum both languages
                    MiniStatCard(
                        icon      = Icons.Filled.Games,
                        label     = stringResource(R.string.profile_stat_played),
                        value     = (uiState.enGamesPlayed + uiState.arGamesPlayed).toString(),
                        accent    = colors.buttonTaupe,
                        modifier  = Modifier.weight(1f),
                    )
                    MiniStatCard(
                        icon      = Icons.Filled.Check,
                        label     = stringResource(R.string.profile_stat_solved),
                        value     = (uiState.enWordsSolved + uiState.arWordsSolved).toString(),
                        accent    = colors.buttonTeal,
                        modifier  = Modifier.weight(1f),
                    )
                    MiniStatCard(
                        icon     = Icons.Outlined.EmojiEvents,
                        label    = stringResource(R.string.profile_stat_win_rate),
                        value    = run {
                            val totalGames  = uiState.enGamesPlayed + uiState.arGamesPlayed
                            val totalSolved = uiState.enWordsSolved + uiState.arWordsSolved
                            val rate = if (totalGames > 0) (totalSolved * 100 / totalGames) else 0
                            "$rate%"
                        },
                        accent    = colors.buttonPink,
                        modifier  = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitles: List<Pair<String, String>> = emptyList(), // label to value
) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(spacing.md))
            .background(accent.copy(alpha = 0.10f))
            .padding(vertical = spacing.md, horizontal = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Box(
            modifier         = Modifier.size(spacing.xxl)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(spacing.md)
            )
        }

        WordleText(
            text       = value,
            color      = colors.title,
            fontSize   = typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )

        WordleText(
            text     = label,
            color    = colors.body.copy(alpha = 0.45f),
            fontSize = typography.labelSmall,
        )

        // Per-language breakdown
        if (subtitles.isNotEmpty()) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.xs))
                    .background(accent.copy(alpha = 0.10f))
                    .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                subtitles.forEachIndexed { index, (lang, stat) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        WordleText(
                            text     = lang,
                            color    = colors.body.copy(alpha = 0.35f),
                            fontSize = typography.labelSmall,
                        )
                        WordleText(
                            text       = stat,
                            color      = accent,
                            fontSize   = typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    // Divider between items
                    if (index < subtitles.lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(spacing.md)
                                .padding(horizontal = spacing.xxs)
                                .background(colors.border)
                                .size(width = 1.dp, height = spacing.md)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileSection(
    editName: String,
    avatarUrl: String?,
    pendingAvatarUri: Uri?,
    onNameChanged: (String) -> Unit,
    onAvatarChanged: (Uri?) -> Unit,
    onSave: Action,
    onCancel: Action,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? -> uri?.let { onAvatarChanged(it) } }

        // Avatar with edit icon
        Box(contentAlignment = Alignment.BottomEnd) {
            val displayModel: Any? = pendingAvatarUri ?: avatarUrl
            if (displayModel != null) {
                AsyncImage(
                    model              = displayModel,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.size(spacing.avatarMd)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(colors.buttonPink, colors.buttonTeal)
                            ),
                            shape = CircleShape
                        )
                        .padding(spacing.xxs)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            } else {
                // Show initials if no avatar is set
                PlayerAvatar(
                    name      = editName.ifBlank { "" },
                    avatarUrl = null,
                    modifier  = Modifier.size(spacing.avatarMd)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(colors.buttonPink, colors.buttonTeal)
                            ),
                            shape = CircleShape
                        )
                        .padding(spacing.xxs)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    fontSize  = typography.headingSmall,
                )
            }
            Box(
                modifier = Modifier.size(spacing.lg)
                    .clip(CircleShape)
                    .background(colors.buttonPink)
                    .border(1.5.dp, colors.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(spacing.sm)
                )
            }
        }

        OutlinedTextField(
            value         = editName,
            onValueChange = onNameChanged,
            label         = { Text(stringResource(R.string.profile_display_name_label), fontSize = typography.labelMedium) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(0.78f),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = colors.buttonTeal,
                unfocusedBorderColor = colors.border,
                focusedTextColor     = colors.title,
                unfocusedTextColor   = colors.title,
                cursorColor          = colors.buttonTeal,
                focusedLabelColor    = colors.buttonTeal,
                unfocusedLabelColor  = colors.body.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(spacing.md)
        )

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.roundFull))
                    .background(Color.Transparent)
                    .border(1.dp, colors.border, RoundedCornerShape(spacing.roundFull))
                    .clickable { onCancel() }
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                WordleText(
                    text       = stringResource(R.string.profile_cancel_button),
                    color      = colors.body,
                    fontSize   = typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.roundFull))
                    .background(colors.buttonPink)
                    .clickable { onSave() }
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                WordleText(
                    text       = stringResource(R.string.profile_save_button),
                    color      = colors.title,
                    fontSize   = typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewProfileScreenLight() {
    ProfileScreen(
        uiState = ProfileUiState(
            name            = "Ahmed Al-Rashid",
            email           = "ahmed@example.com",
            enGamesPlayed   = 42,
            enWordsSolved   = 35,
            enWinPercentage = 78,
            enCurrentPoints = 24500,
            isEditMode      = false,
        ),
        uiEffect           = MutableSharedFlow(),
        onBack             = {},
        onSettingsClick    = {},
        onEditProfileClick = {},
        onSaveProfileClick = {},
        onCancelEditClick  = {},
        onNameChanged      = {},
        onAvatarChanged    = {},
    )
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewProfileScreenEditMode() {
    ProfileScreen(
        uiState = ProfileUiState(
            name            = "Ahmed Al-Rashid",
            email           = "ahmed@example.com",
            enGamesPlayed   = 42,
            enWordsSolved   = 35,
            enWinPercentage = 78,
            enCurrentPoints = 24500,
            isEditMode      = true,
            editName        = "Ahmed Al-Rashid",
        ),
        uiEffect           = MutableSharedFlow(),
        onBack             = {},
        onSettingsClick    = {},
        onEditProfileClick = {},
        onSaveProfileClick = {},
        onCancelEditClick  = {},
        onNameChanged      = {},
        onAvatarChanged    = {},
    )
}