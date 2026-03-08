package com.wordle.core.presentation.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.core.presentation.theme.WordleColors

data class PodiumPlayer(
    val name: String,
    val points: Int,
    val avatarUrl: String? = null,
)

@Composable
fun TopThreePodium(
    first: PodiumPlayer,
    second: PodiumPlayer,
    third: PodiumPlayer,
    modifier: Modifier = Modifier,
) {
    val colors = LocalWordleColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 2nd place
        PodiumEntry(
            rank       = 2,
            player     = second,
            avatarSize = 72.dp,
            isFirst    = false,
            colors     = colors,
        )

        // 1st place — elevated with crown
        PodiumEntry(
            rank       = 1,
            player     = first,
            avatarSize = 90.dp,
            isFirst    = true,
            colors     = colors,
        )

        // 3rd place
        PodiumEntry(
            rank       = 3,
            player     = third,
            avatarSize = 72.dp,
            isFirst    = false,
            colors     = colors,
        )
    }
}

@Composable
private fun PodiumEntry(
    rank: Int,
    player: PodiumPlayer,
    avatarSize: Dp,
    isFirst: Boolean,
    colors: WordleColors,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Crown for 1st place
        if (isFirst) {
            Text(
                text     = "👑",
                fontSize = 28.sp,
                modifier = Modifier.offset(y = 6.dp)
            )
        } else {
            // Rank number above avatar for 2nd and 3rd
            Text(
                text       = rank.toString(),
                color      = colors.body,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(colors.surface)
                .border(
                    width = if (isFirst) 3.dp else 2.dp,
                    color = if (isFirst) colors.present else colors.border,
                    shape = CircleShape
                )
        ) {
            if (player.avatarUrl != null) {
                AsyncImage(
                    model= player.avatarUrl,
                    contentDescription = "${player.name} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                )
            } else {
                val initials = player.name
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                Text(
                    text = initials,
                    color = colors.title,
                    fontSize = if (isFirst) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(2.dp))

        // Points
        Text(
            text       = "%,d".format(player.points),
            color      = colors.present,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
        )
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