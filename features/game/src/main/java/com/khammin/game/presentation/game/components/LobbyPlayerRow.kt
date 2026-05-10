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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.R as CoreRes

@Composable
fun LobbyPlayerRow(
    name: String,
    badge: String? = null,
    badgeColor: Color = colors.buttonTeal,
    avatarColor: Long? = null,
    avatarEmoji: String? = null,
    avatarUrl: String? = null,
    isAfk: Boolean = false,
    afkCountdown: Int? = null,
    level: Int? = null,
) {
    // ── Dot color ─────────────────────────────────────────────
    val dotColor = if (isAfk) Color(0xFF9E9E9E) else Color(0xFF4CAF50)

    // ── Card ──────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation       = 2.dp,
                shape           = RoundedCornerShape(16.dp),
                ambientColor    = Color.Black.copy(alpha = 0.06f),
                spotColor       = Color.Black.copy(alpha = 0.06f),
            )
            .background(colors.background, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        // ── Left: badge ───────────────────────────────────────
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(0.5.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = badge,
                    color      = badgeColor,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ── Center: name + level + countdown ─────────────────
        Column(
            modifier            = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text       = name,
                color      = colors.title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            if (level != null) {
                Text(
                    text     = "مستوى $level",
                    color    = colors.body.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                )
            }
            // Show countdown only after 30s AFK
            if (isAfk && afkCountdown != null && afkCountdown <= 30) {
                val m = afkCountdown / 60
                val s = afkCountdown % 60
                Text(
                    text       = stringResource(
                        CoreRes.string.lobby_disconnected_after,
                        m.toString(),
                        s.toString().padStart(2, '0')
                    ),
                    color      = Color(0xFFFF5252),
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // ── Right: avatar + status dot ────────────────────────
        Box(contentAlignment = Alignment.BottomEnd) {

            // Avatar
            when {
                avatarUrl != null -> AsyncImage(
                    model          = avatarUrl,
                    contentDescription = name,
                    contentScale   = ContentScale.Crop,
                    modifier       = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                )
                avatarColor != null && avatarEmoji != null -> Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(avatarColor).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(avatarColor).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatarEmoji, fontSize = 20.sp)
                }
                avatarColor != null -> {
                    val c = Color(avatarColor)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(c.copy(alpha = 0.15f))
                            .border(1.5.dp, c.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = name.take(1).uppercase(),
                            color      = c,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                else -> Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.5.dp, badgeColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = name.take(1).uppercase(),
                        color      = badgeColor,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Status dot — overlay at bottom-right of avatar
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(colors.background, CircleShape)
                    .padding(2.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@GameLightBackgroundPreview
@Composable
fun LobbyPlayerRowPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LobbyPlayerRow(
            name        = "Alex",
            badge       = "المضيف",
            badgeColor  = Color(0xFF00BCD4),
            avatarColor = 0xFF00BCD4,
            avatarEmoji = "🐱",
            level       = 42,
            isAfk       = false,
        )
        LobbyPlayerRow(
            name        = "BigWordie",
            badge       = "ينتظر",
            badgeColor  = Color(0xFF9E9E9E),
            avatarColor = 0xFF9E9E9E,
            level       = 28,
            isAfk       = true,
        )
        LobbyPlayerRow(
            name        = "صالح",
            badge       = "مستعد",
            badgeColor  = Color(0xFF4CAF50),
            avatarColor = 0xFF4CAF50,
            level       = 15,
            isAfk       = false,
        )
    }
}