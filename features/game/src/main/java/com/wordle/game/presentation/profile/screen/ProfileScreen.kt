package com.wordle.game.presentation.profile.screen

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerAvatar
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
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

    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7faf68),
            Color(0xFFecf0ea),
        )
    )

    val waveShape = GenericShape { size, _ ->
        val waveHeight = 60f
        val waveCount  = 2
        val waveWidth  = size.width / waveCount

        moveTo(0f, waveHeight)

        for (i in 0 until waveCount) {
            val startX = i * waveWidth
            cubicTo(
                startX + waveWidth * 0.25f, 0f,
                startX + waveWidth * 0.75f, waveHeight * 2f,
                startX + waveWidth,          waveHeight,
            )
        }

        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen gradient background ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(headerGradient)
        )

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector        = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector        = Icons.Filled.Settings,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                },
                colors   = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (isEditMode) {
                EditProfileSection(
                    editName         = editName,
                    avatarUrl        = avatarUrl,
                    pendingAvatarUri = pendingAvatarUri,
                    onNameChanged    = onNameChanged,
                    onAvatarChanged  = onAvatarChanged,
                    onSave           = onSaveProfileClick,
                    onCancel         = onCancelEditClick,
                    modifier         = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                PlayerAvatar(
                    name      = name,
                    avatarUrl = avatarUrl,
                    modifier  = Modifier.size(88.dp),
                    fontSize  = 28.sp,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = name,
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text       = email,
                    color      = Color.White.copy(alpha = 0.75f),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Normal,
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
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
                            tint               = Color.White,
                            modifier           = Modifier.size(14.dp)
                        )
                        Text(
                            text       = "Edit Profile",
                            color      = Color.White,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        // ── Wave bottom sheet ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .clip(waveShape)
                .background(Color(0xFFF8FAF7))
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 28.dp)
        ) {
            Text(
                text          = "Statistics",
                color         = colors.title,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier          = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Games Played", gamesPlayed.toString(),       Modifier.fillMaxWidth())
                StatCard("Words Solved", wordsSolved.toString(),       Modifier.fillMaxWidth())
                StatCard("Win Rate",     "$winPercentage%",            Modifier.fillMaxWidth())
                StatCard("Points",       "%,d".format(currentPoints), Modifier.fillMaxWidth())
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
    modifier: Modifier = Modifier
) {
    val colors = LocalWordleColors.current

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onAvatarChanged(it) }
        }

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier         = Modifier.wrapContentSize()
        ) {
            val displayModel: Any? = pendingAvatarUri ?: avatarUrl

            if (displayModel != null) {
                AsyncImage(
                    model              = displayModel,
                    contentDescription = "Avatar preview",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
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
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF7faf68).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.CameraAlt,
                    contentDescription = "Change avatar",
                    tint               = Color(0xFF7faf68),
                    modifier           = Modifier.size(13.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value         = editName,
            onValueChange = onNameChanged,
            label         = { Text("Display Name", fontSize = 12.sp) },
            singleLine    = true,
            modifier = Modifier.fillMaxWidth(0.75f),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White,
                cursorColor          = Color.White,
                focusedLabelColor    = Color.White,
                unfocusedLabelColor  = Color.White.copy(alpha = 0.7f),
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "Cancel",
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .clickable { onSave() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "Save",
                    color      = Color(0xFF7faf68),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val splitShape = GenericShape { size, _ ->
        moveTo(size.width * 0.55f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(size.width * 0.45f, size.height)
        close()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
            .height(64.dp)
    ) {
        // ── Left side — light green background ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEDF7E8))
        )

        // ── Right side — dark green diagonal fill ─────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(splitShape)
                .background(Color(0xFF7faf68).copy(alpha = 0.15f))
        )

        // ── Label on the left ─────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = label,
                color      = Color.Black.copy(alpha = 0.55f),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // ── Value on the right ────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text          = value,
                color         = Color(0xFF7faf68),
                fontSize      = 15.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            )
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