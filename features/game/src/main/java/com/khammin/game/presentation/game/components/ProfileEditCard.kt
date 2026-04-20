package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun ProfileEditCard(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    onSave: (name: String, color: Long?, emoji: String?) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(myName) { mutableStateOf(myName) }
    // null = user selected the default letter-based avatar
    var draftColor by remember(avatarColor) { mutableStateOf(avatarColor) }
    var draftEmoji by remember(avatarEmoji) { mutableStateOf(avatarEmoji) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val previewColor = if (isEditing) draftColor else avatarColor
                val previewEmoji = if (isEditing) draftEmoji else avatarEmoji
                val previewName  = if (isEditing) draftName else myName
                when {
                    previewColor != null && previewEmoji != null ->
                        EmojiAvatar(color = previewColor, emoji = previewEmoji, size = 44)
                    previewColor != null -> {
                        // Color chosen, emoji still "first letter of name"
                        val circleColor = Color(previewColor)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(circleColor.copy(alpha = 0.20f))
                                .border(1.dp, circleColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = previewName.take(1).uppercase().ifBlank { "?" },
                                color = circleColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    else -> {
                        // Default: no color, no emoji — plain letter circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.buttonPink.copy(alpha = 0.15f))
                                .border(1.dp, colors.buttonPink.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = previewName.take(1).uppercase().ifBlank { "?" },
                                color = colors.buttonPink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = (if (isEditing) draftName else myName).ifBlank { "You" },
                        color = colors.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "You",
                        color = colors.body.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                }
            }
            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.buttonTeal.copy(alpha = 0.12f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { isEditing = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Edit",
                        color = colors.buttonTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ── Inline edit mode ─────────────────────────────────────────────────
        if (isEditing) {
            // Name field
            OutlinedTextField(
                value = draftName,
                onValueChange = { if (it.length <= 15) draftName = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.buttonTeal,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.buttonTeal,
                    unfocusedLabelColor = colors.body.copy(alpha = 0.5f),
                    focusedTextColor = colors.title,
                    unfocusedTextColor = colors.title,
                    cursorColor = colors.buttonTeal,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
            )

            // Color picker
            Text(
                text = "Avatar color",
                color = colors.body.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Default slot — letter circle
                item {
                    val selected = draftColor == null
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.buttonTeal.copy(alpha = 0.15f))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) colors.title else colors.buttonTeal.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { draftColor = null; draftEmoji = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = draftName.take(1).uppercase().ifBlank { "?" },
                            color = colors.buttonTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(avatarColorOptions) { colorLong ->
                    val selected = colorLong == draftColor
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (selected) 2.5.dp else 0.dp,
                                color = if (selected) colors.title else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                draftColor = colorLong
                            },
                    )
                }
            }

            // Emoji picker — hidden when default (null) color is selected
            if (draftColor != null) {
                Text(
                    text = "Avatar emoji",
                    color = colors.body.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Default slot — first letter of name
                    item {
                        val selected = draftEmoji == null
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) colors.buttonTeal.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { draftEmoji = null },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = draftName.take(1).uppercase().ifBlank { "?" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) colors.buttonTeal else colors.body,
                            )
                        }
                    }
                    items(avatarEmojiOptions) { emoji ->
                        val selected = emoji == draftEmoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) colors.buttonTeal.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { draftEmoji = emoji },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Save / Close buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        draftName = myName
                        draftColor = avatarColor
                        draftEmoji = avatarEmoji
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surface,
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                ) {
                    Text(
                        text = "Close",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.body,
                    )
                }
                Button(
                    onClick = {
                        onSave(draftName, draftColor, draftEmoji)
                        isEditing = false
                    },
                    enabled = draftName.isNotBlank(),
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonTeal,
                        disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text = "Save",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background,
                    )
                }
            }
        }
    }
}

@Composable
fun EmojiAvatar(color: Long, emoji: String, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size * 0.52f).sp)
    }
}

private val avatarColorOptions = listOf(
    0xFFE53935L, // Red
    0xFFFB8C00L, // Orange
    0xFFFDD835L, // Yellow
    0xFF43A047L, // Green
    0xFF00897BL, // Teal
    0xFF1E88E5L, // Blue
    0xFF8E24AAL, // Purple
    0xFFD81B60L, // Pink
    0xFF6D4C41L, // Brown
    0xFF757575L, // Gray
)

private val avatarEmojiOptions = listOf(
    "😎", "🐱", "🦊", "🐼", "🎮", "⭐", "🔥", "🌙", "🎯", "🦁",
    "🐸", "🤖", "👾", "🎲", "🌈", "🦋", "🐧", "🦄", "🎪", "🎭",
)
