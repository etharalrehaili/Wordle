package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R as CoreRes
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.OpponentProgress

private data class LeaderboardPlayer(
    val name: String,
    val avatarColor: Long?,
    val avatarEmoji: String?,
    val sessionPoints: Int,
    val isMe: Boolean,
)

@Composable
fun GuestGameOverLobby(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    targetWord: String,
    opponentsProgress: Map<String, OpponentProgress>,
    wordLength: Int,
    roundNumber: Int = 1,
    myName: String = "You",
    myAvatarColor: Long? = null,
    myAvatarEmoji: String? = null,
    myGuessCount: Int = 0,
    myTotalPoints: Int = 0,
    sessionPoints: Map<String, Int> = emptyMap(),
    hasVotedPlayAgain: Boolean,
    onVotePlayAgain: () -> Unit
) {
    val accentColor = if (isWin) colors.buttonTeal else colors.buttonPink

    // Build ranked leaderboard: me + all opponents, sorted by sessionPoints desc
    val meEntry = LeaderboardPlayer(
        name          = myName.ifBlank { "You" },
        avatarColor   = myAvatarColor,
        avatarEmoji   = myAvatarEmoji,
        sessionPoints = if (sessionPoints.isNotEmpty()) myTotalPoints else myTotalPoints,
        isMe          = true,
    )
    val opponentEntries = opponentsProgress.entries.map { (userId, progress) ->
        LeaderboardPlayer(
            name          = progress.name,
            avatarColor   = progress.avatarColor,
            avatarEmoji   = progress.avatarEmoji,
            sessionPoints = sessionPoints[userId] ?: progress.totalPoints,
            isMe          = false,
        )
    }
    val ranked = (listOf(meEntry) + opponentEntries)
        .sortedByDescending { it.sessionPoints }

    LazyColumn(
        modifier            = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Result header ─────────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text          = "Round $roundNumber",
                    color         = colors.buttonTeal,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Text(text = if (isWin) "🎉" else "🤔", fontSize = 48.sp)
                Text(
                    text       = if (isWin) stringResource(CoreRes.string.spectator_result_you_guessed)
                    else       stringResource(CoreRes.string.result_lose_title),
                    color      = accentColor,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )
                if (targetWord.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = stringResource(CoreRes.string.result_the_word_was),
                        color     = colors.body.copy(alpha = 0.55f),
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        targetWord.forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = letter.toString(),
                                    color      = accentColor,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Session leaderboard ───────────────────────────────────────────────
        item {
            Text(
                text          = "Leaderboard",
                color         = colors.body.copy(alpha = 0.5f),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                ranked.forEachIndexed { index, player ->
                    val rank = index + 1
                    val rankLabel = when (rank) {
                        1    -> "🥇"
                        2    -> "🥈"
                        3    -> "🥉"
                        else -> "$rank."
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (player.isMe) Modifier.background(colors.buttonTeal.copy(alpha = 0.07f))
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text      = rankLabel,
                                fontSize  = if (rank <= 3) 20.sp else 13.sp,
                                modifier  = Modifier.width(28.dp),
                                textAlign = TextAlign.Center,
                            )
                            // Avatar
                            when {
                                player.avatarColor != null && player.avatarEmoji != null ->
                                    EmojiAvatar(color = player.avatarColor, emoji = player.avatarEmoji, size = 32)
                                player.avatarColor != null -> {
                                    val circleColor = Color(player.avatarColor)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(circleColor.copy(alpha = 0.20f))
                                            .border(1.dp, circleColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text       = player.name.take(1).uppercase(),
                                            color      = circleColor,
                                            fontSize   = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                                else -> Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(colors.buttonTeal.copy(alpha = 0.15f))
                                        .border(1.dp, colors.buttonTeal.copy(alpha = 0.3f), RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text       = player.name.take(1).uppercase(),
                                        color      = colors.buttonTeal,
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(
                                text       = player.name,
                                color      = colors.title,
                                fontSize   = 14.sp,
                                fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (player.isMe) {
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
                            Text(
                                text       = "${player.sessionPoints} pts",
                                color      = colors.title,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // ── Play again vote button ────────────────────────────────────────────
        item {
            Button(
                onClick  = onVotePlayAgain,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
                    .padding(horizontal = 32.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (hasVotedPlayAgain) colors.buttonTeal
                    else colors.surface,
                ),
            ) {
                Text(
                    text       = if (hasVotedPlayAgain) "✓  Voted to play again" else "👋  Play Again?",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (hasVotedPlayAgain) colors.background else colors.body,
                )
            }
        }

        // ── Waiting indicator ─────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(text = "⏳", fontSize = 22.sp)
                Text(
                    text      = "Waiting for host to start the next round…",
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
