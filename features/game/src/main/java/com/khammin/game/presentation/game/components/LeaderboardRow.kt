package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.theme.GameDesignTheme.colors

data class LeaderboardEntry(
    val userId: String,
    val name: String,
    val avatarColor: Long? = null,
    val avatarEmoji: String? = null,
    val sessionPoints: Int = 0,
    val guessRows: List<GuessRow> = List(MAX_GUESSES) { GuessRow() },
    val roundSolved: Boolean = false,
    val roundFailed: Boolean = false,
    val roundGuessCount: Int = 0,
    val isMe: Boolean = false,
    val isHost: Boolean = false,
)

@Composable
fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    wordLength: Int,
    modifier: Modifier = Modifier,
) {
    val rankLabel = when (rank) {
        1    -> "🥇"
        2    -> "🥈"
        3    -> "🥉"
        else -> "$rank."
    }
    val roundBadgeText = when {
        entry.roundSolved -> "Solved in ${entry.roundGuessCount}"
        entry.roundFailed -> "Failed"
        else              -> null
    }
    val roundBadgeColor = if (entry.roundSolved) colors.buttonTeal else colors.buttonPink

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (entry.isMe) Modifier.background(colors.buttonTeal.copy(alpha = 0.07f))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: rank + GuestCard
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text      = rankLabel,
                fontSize  = if (rank <= 3) 20.sp else 13.sp,
                modifier  = Modifier.width(30.dp),
                textAlign = TextAlign.Center,
            )
            GuestCard(
                name        = entry.name,
                avatarColor = entry.avatarColor,
                avatarEmoji = entry.avatarEmoji,
                guesses     = entry.guessRows,
                wordLength  = wordLength,
            )
        }

        // Right: points + badges
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!entry.isHost) {
                Text(
                    text       = "${entry.sessionPoints} pts",
                    color      = colors.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (roundBadgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(roundBadgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text       = roundBadgeText,
                        color      = roundBadgeColor,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (entry.isMe) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.buttonPink.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text       = "You",
                        color      = colors.buttonPink,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
