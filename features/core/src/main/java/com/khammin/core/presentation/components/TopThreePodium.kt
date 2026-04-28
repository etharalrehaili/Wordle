package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.WordleColors

data class PodiumPlayer(
    val name: String,
    val points: Int,
    val avatarUrl: String? = null,
    val isMe: Boolean = false,
)

@Composable
fun TopThreePodium(
    modifier: Modifier = Modifier,
    first: PodiumPlayer,
    second: PodiumPlayer? = null,
    third: PodiumPlayer? = null
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 2nd place — empty slot if null
        if (second != null) {
            PodiumEntry(rank = 2, player = second, avatarSize = 68.dp, isFirst = false, accentColor = colors.buttonTaupe, colors = colors)
        } else {
            EmptyPodiumSlot(rank = 2, colors = colors)
        }

        // 1st place — always present
        PodiumEntry(rank = 1, player = first, avatarSize = 88.dp, isFirst = true, accentColor = colors.present, colors = colors)

        // 3rd place — empty slot if null
        if (third != null) {
            PodiumEntry(rank = 3, player = third, avatarSize = 68.dp, isFirst = false, accentColor = colors.buttonTaupe, colors = colors)
        } else {
            EmptyPodiumSlot(rank = 3, colors = colors)
        }
    }
}

@Composable
private fun EmptyPodiumSlot(rank: Int, colors: WordleColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier            = Modifier.padding(horizontal = 6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.buttonTaupe.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%d", rank),
                color      = colors.buttonTaupe.copy(alpha = 0.35f),
                fontSize   = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Dashed empty avatar circle
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.buttonTaupe.copy(alpha = 0.25f),
                            colors.buttonTaupe.copy(alpha = 0.10f),
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", color = colors.buttonTaupe.copy(alpha = 0.30f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text     = "—",
            color    = colors.body.copy(alpha = 0.20f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PodiumEntry(
    rank: Int,
    player: PodiumPlayer,
    avatarSize: Dp,
    isFirst: Boolean,
    accentColor: Color,
    colors: WordleColors,
) {

    val resolvedAccent = if (player.isMe) colors.buttonTeal else accentColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier            = Modifier.padding(horizontal = 6.dp)
    ) {
        // Crown / rank badge
        if (isFirst) {
            Text(
                text     = "👑",
                fontSize = 26.sp,
                modifier = Modifier.offset(y = 8.dp)
            )
        } else {
            Box(
                modifier         = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(resolvedAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%d", rank),
                    color      = resolvedAccent,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Avatar with gradient border
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .border(
                    width = if (isFirst) 3.dp else 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(resolvedAccent, resolvedAccent.copy(alpha = 0.4f))
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
        ) {
            PlayerAvatar(
                name      = player.name,
                avatarUrl = player.avatarUrl,
                modifier  = Modifier.size(avatarSize),
                fontSize  = if (isFirst) 22.sp else 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name
        Text(
            text       = player.name,
            color      = colors.title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Points pill
        Box(
            modifier         = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(resolvedAccent.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%,d", player.points),
                color      = resolvedAccent,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun TopThreePodiumPreview() {
    TopThreePodium(
        first  = PodiumPlayer(name = "Ahmed Al-Rashid", points = 24500),
        second = PodiumPlayer(name = "Sarah Jenkins",   points = 11820),
        third  = PodiumPlayer(name = "Omar Hassan",     points = 9340),
    )
}