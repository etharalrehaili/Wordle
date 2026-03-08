package com.wordle.game.presentation

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerAvatar
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors

@Composable
fun ProfileScreen(
    name: String,
    avatarUrl: String?,
    gamesPlayed: Int,
    wordsSolved: Int,
    winPercentage: Int,
    currentPoints: Int,
    isEditMode: Boolean,
    editName: String,
    onBack: Action,
    onSettingsClick: Action,
    onEditProfileClick: Action,
    onSaveProfileClick: Action,
    onCancelEditClick: Action,
    onNameChanged: (String) -> Unit,
    onAvatarChanged: (String?) -> Unit,
) {
    val colors = LocalWordleColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        GameTopBar(
            title              = "Profile",
            startIcon          = Icons.Filled.ArrowBack,
            onStartIconClicked = onBack,
            endIcon            = Icons.Filled.Settings,
            onEndIconClicked   = onSettingsClick,
            modifier           = Modifier.fillMaxWidth()
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isEditMode) {
                // ── Edit Mode ────────────────────────────────────────────────
                EditProfileSection(
                    editName        = editName,
                    avatarUrl       = avatarUrl,
                    onNameChanged   = onNameChanged,
                    onAvatarChanged = onAvatarChanged,
                    onSave          = onSaveProfileClick,
                    onCancel        = onCancelEditClick,
                )
            } else {
                // ── View Mode ────────────────────────────────────────────────
                PlayerAvatar(
                    name      = name,
                    avatarUrl = avatarUrl,
                    modifier  = Modifier.size(96.dp),
                    fontSize  = 28.sp,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = name,
                    color      = colors.title,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(Modifier.height(24.dp))

                GameButton(
                    label           = "Edit Profile",
                    icon            = Icons.Filled.Edit,
                    backgroundColor = colors.key,
                    contentColor    = colors.title,
                    onClick         = onEditProfileClick,
                    modifier        = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Spacer(Modifier.height(32.dp))

            // ── Statistics (always visible) ──────────────────────────────────
            Text(
                text          = "Statistics",
                color         = colors.title,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier      = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Games Played", gamesPlayed.toString(), Modifier.weight(1f))
                StatCard("Words Solved", wordsSolved.toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Win Rate", "$winPercentage%", Modifier.weight(1f))
                StatCard("Points", "%,d".format(currentPoints), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EditProfileSection(
    editName: String,
    avatarUrl: String?,
    onNameChanged: (String) -> Unit,
    onAvatarChanged: (String?) -> Unit,
    onSave: Action,
    onCancel: Action,
) {
    val colors = LocalWordleColors.current

    // Tappable avatar with a camera-badge overlay
    Box(contentAlignment = Alignment.BottomEnd) {
        PlayerAvatar(
            name      = editName.ifBlank { "?" },
            avatarUrl = avatarUrl,
            modifier  = Modifier
                .size(96.dp)
                .clickable {
                    // TODO: launch image picker and call onAvatarChanged(uri)
                },
            fontSize  = 28.sp,
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.key)
                .border(2.dp, colors.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.CameraAlt,
                contentDescription = "Change avatar",
                tint               = colors.title,
                modifier           = Modifier.size(14.dp)
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    // Name text field
    OutlinedTextField(
        value         = editName,
        onValueChange = onNameChanged,
        label         = { Text("Display Name", color = colors.body) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = colors.title,
            unfocusedBorderColor = colors.border,
            focusedTextColor     = colors.title,
            unfocusedTextColor   = colors.title,
            cursorColor          = colors.title,
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(16.dp))

    // Save / Cancel
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameButton(
            label           = "Cancel",
            backgroundColor = colors.surface,
            contentColor    = colors.body,
            onClick         = onCancel,
            modifier        = Modifier.weight(1f)
        )
        GameButton(
            label           = "Save",
            backgroundColor = colors.title,
            contentColor    = colors.background,
            onClick         = onSave,
            modifier        = Modifier.weight(1f)
        )
    }
}

// ─── Stat Card ───────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWordleColors.current

    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = value,
            color      = colors.title,
            fontSize   = 32.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = label,
            color      = colors.body,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@GameLightBackgroundPreview
@Composable
private fun PreviewProfileScreenLight() {
    ProfileScreen(
        name               = "Ahmed Al-Rashid",
        avatarUrl          = null,
        gamesPlayed        = 42,
        wordsSolved        = 35,
        winPercentage      = 78,
        currentPoints      = 24500,
        isEditMode         = false,
        editName           = "",
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
        name               = "Ahmed Al-Rashid",
        avatarUrl          = null,
        gamesPlayed        = 42,
        wordsSolved        = 35,
        winPercentage      = 78,
        currentPoints      = 24500,
        isEditMode         = true,
        editName           = "Ahmed Al-Rashid",
        onBack             = {},
        onSettingsClick    = {},
        onEditProfileClick = {},
        onSaveProfileClick = {},
        onCancelEditClick  = {},
        onNameChanged      = {},
        onAvatarChanged    = {},
    )
}