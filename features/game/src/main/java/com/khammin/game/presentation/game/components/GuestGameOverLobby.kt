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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R as CoreRes
import com.khammin.core.presentation.components.ConfettiLayer
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
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
    myUserId: String = "",
    myGuessCount: Int = 0,
    myTotalPoints: Int = 0,
    sessionPoints: Map<String, Int> = emptyMap(),
) {
    val accentColor = if (isWin) colors.logoGreen else colors.logoPink

    val meEntry = LeaderboardPlayer(
        name          = myName.ifBlank { stringResource(R.string.lobby_you) },
        avatarColor   = myAvatarColor,
        avatarEmoji   = myAvatarEmoji,
        sessionPoints = myTotalPoints,
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
    val ranked = (listOf(meEntry) + opponentEntries).sortedByDescending { it.sessionPoints }

    Box(modifier = modifier) {
        if (isWin) {
            ConfettiLayer(modifier = Modifier.matchParentSize())
        }

        LazyColumn(
            modifier            = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Result header ─────────────────────────────────────────────────
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text          = stringResource(R.string.lobby_round, String.format(Locale.US, "%d", roundNumber)),
                        color         = colors.logoBlue,
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text       = if (isWin) stringResource(CoreRes.string.spectator_result_you_guessed)
                        else stringResource(CoreRes.string.result_lose_title),
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

            // ── Leaderboard label ─────────────────────────────────────────────
            item {
                Text(
                    text          = stringResource(R.string.lobby_leaderboard),
                    color         = colors.body.copy(alpha = 0.5f),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier      = Modifier.fillMaxWidth(),
                )
            }

            // ── Leaderboard rows ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                ) {
                    ranked.forEachIndexed { index, player ->
                        val rank      = index + 1
                        val rankLabel = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "${String.format(Locale.US, "%d", rank)}." }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (player.isMe) Modifier.background(colors.logoBlue.copy(alpha = 0.07f))
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
                                when {
                                    player.avatarColor != null && player.avatarEmoji != null ->
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(player.avatarColor).copy(alpha = 0.20f))
                                                .border(1.dp, Color(player.avatarColor).copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text     = player.avatarEmoji,
                                                fontSize = 16.sp,
                                            )
                                        }
                                    player.avatarColor != null -> {
                                        val c = Color(player.avatarColor)
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(c.copy(alpha = 0.20f))
                                                .border(1.dp, c.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text       = player.name.take(1).uppercase(),
                                                color      = c,
                                                fontSize   = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                    else -> Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(colors.logoBlue.copy(alpha = 0.15f))
                                            .border(1.dp, colors.logoBlue.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text       = player.name.take(1).uppercase(),
                                            color      = colors.logoBlue,
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
                                            .background(colors.logoPink.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text       = stringResource(R.string.lobby_badge_you_label),
                                            color      = colors.logoPink,
                                            fontSize   = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                                Text(
                                    text       = String.format(Locale.US, stringResource(R.string.lobby_pts), player.sessionPoints),
                                    color      = colors.title,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            // ── Waiting indicator ─────────────────────────────────────────────
            item {
                Text(
                    text      = stringResource(R.string.lobby_waiting_next_round),
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
