package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun LobbyPlayerRow(
    name: String,
    badge: String? = null,
    badgeColor: Color = colors.buttonTeal,
    avatarColor: Long? = null,
    avatarEmoji: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                avatarColor != null && avatarEmoji != null ->
                    EmojiAvatar(color = avatarColor, emoji = avatarEmoji, size = 32)
                avatarColor != null -> {
                    val circleColor = Color(avatarColor)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(circleColor.copy(alpha = 0.20f))
                            .border(1.dp, circleColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).uppercase(),
                            color = circleColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                else -> Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        color = badgeColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = name,
                color = colors.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@GameLightBackgroundPreview
@Composable
fun LobbyPlayerRowPreview() {
    Column {
        // With emoji avatar and badge
        LobbyPlayerRow(
            name = "Alice",
            badge = "HOST",
            badgeColor = Color(0xFF00BCD4),
            avatarColor = 0xFF00BCD4,
            avatarEmoji = "🐱",
        )
        // With color avatar (no emoji)
        LobbyPlayerRow(
            name = "Bob",
            badge = "READY",
            badgeColor = Color(0xFF4CAF50),
            avatarColor = 0xFF4CAF50,
        )
        // Fallback avatar (no color, no emoji)
        LobbyPlayerRow(
            name = "Charlie",
        )
    }
}