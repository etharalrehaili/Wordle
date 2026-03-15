package com.wordle.game.presentation.profile.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerAvatar
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.GameDesignTheme
import com.wordle.core.presentation.theme.GameDesignTheme.colors
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String,
    email: String,
    avatarUrl: String?,
    pendingAvatarUri: Uri?,
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
    onAvatarChanged: (Uri?) -> Unit,
) {
    val colors = LocalWordleColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.buttonPink.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(top = 52.dp, bottom = 28.dp)
                    .padding(horizontal = 24.dp)
            ) {
                // Back button
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.align(Alignment.TopStart).size(40.dp)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint               = colors.body,
                        modifier           = Modifier.size(22.dp)
                    )
                }

                // Settings button
                IconButton(
                    onClick  = onSettingsClick,
                    modifier = Modifier.align(Alignment.TopEnd).size(40.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Settings,
                        contentDescription = null,
                        tint               = colors.body,
                        modifier           = Modifier.size(22.dp)
                    )
                }

                // Avatar + name centered
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(40.dp))

                    if (isEditMode) {
                        EditProfileSection(
                            editName         = editName,
                            avatarUrl        = avatarUrl,
                            pendingAvatarUri = pendingAvatarUri,
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
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(colors.buttonPink, colors.buttonTeal)
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                            ) {
                                PlayerAvatar(
                                    name      = name,
                                    avatarUrl = avatarUrl,
                                    modifier  = Modifier.fillMaxSize(),
                                    fontSize  = 28.sp,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        WordleText(
                            text       = name,
                            color      = colors.title,
                            fontSize   = GameDesignTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                        )

                        Spacer(Modifier.height(4.dp))

                        WordleText(
                            text     = email,
                            color    = colors.body.copy(alpha = 0.5f),
                            fontSize = GameDesignTheme.typography.labelSmall,
                        )

                        Spacer(Modifier.height(16.dp))

                        // Edit button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colors.buttonPink.copy(alpha = 0.12f))
                                .border(1.dp, colors.buttonPink.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                                .clickable { onEditProfileClick() }
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint               = colors.buttonPink,
                                    modifier           = Modifier.size(13.dp)
                                )
                                WordleText(
                                    text       = "Edit Profile",
                                    color      = colors.buttonPink,
                                    fontSize   = GameDesignTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            // ── Stats ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Points banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colors.buttonPink.copy(alpha = 0.20f),
                                    colors.buttonTeal.copy(alpha = 0.20f),
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            WordleText(
                                text       = "Total Points",
                                color      = colors.body.copy(alpha = 0.55f),
                                fontSize   = GameDesignTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp,
                            )
                            Spacer(Modifier.height(2.dp))
                            WordleText(
                                text       = "%,d".format(currentPoints),
                                color      = colors.title,
                                fontSize   = GameDesignTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.buttonPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Star,
                                contentDescription = null,
                                tint               = colors.buttonPink,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Stats row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard(
                        icon    = Icons.Filled.Games,
                        label   = "Played",
                        value   = gamesPlayed.toString(),
                        accent  = colors.buttonTaupe,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon    = Icons.Filled.Check,
                        label   = "Solved",
                        value   = wordsSolved.toString(),
                        accent  = colors.buttonTeal,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon    = Icons.Outlined.EmojiEvents,
                        label   = "Win Rate",
                        value   = "$winPercentage%",
                        accent  = colors.buttonPink,
                        modifier = Modifier.weight(1f)
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
) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accent,
                modifier           = Modifier.size(20.dp)
            )
        }
        WordleText(
            text       = value,
            color      = colors.title,
            fontSize   = GameDesignTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        WordleText(
            text     = label,
            color    = colors.body.copy(alpha = 0.45f),
            fontSize = GameDesignTheme.typography.labelSmall,
        )
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
    val colors = LocalWordleColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? -> uri?.let { onAvatarChanged(it) } }

        Box(contentAlignment = Alignment.BottomEnd) {
            val displayModel: Any? = pendingAvatarUri ?: avatarUrl
            if (displayModel != null) {
                AsyncImage(
                    model              = displayModel,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(2.dp, colors.buttonPink, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                )
            } else {
                PlayerAvatar(
                    name      = editName.ifBlank { "" },
                    avatarUrl = null,
                    modifier  = Modifier
                        .size(88.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    fontSize  = 28.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colors.buttonPink)
                    .border(1.5.dp, colors.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(13.dp)
                )
            }
        }

        OutlinedTextField(
            value         = editName,
            onValueChange = onNameChanged,
            label         = { Text("Display Name", fontSize = 12.sp) },
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
            shape = RoundedCornerShape(16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Transparent)
                    .border(1.dp, colors.border, RoundedCornerShape(50.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                WordleText(
                    text       = "Cancel",
                    color      = colors.body,
                    fontSize   = GameDesignTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(colors.buttonTeal)
                    .clickable { onSave() }
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                WordleText(
                    text       = "Save",
                    color      = colors.title,
                    fontSize   = GameDesignTheme.typography.labelMedium,
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
        name               = "Ahmed Al-Rashid",
        email              = "ahmed@example.com",
        avatarUrl          = null,
        pendingAvatarUri   = null,
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
        email              = "ahmed@example.com",
        avatarUrl          = null,
        pendingAvatarUri   = null,
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