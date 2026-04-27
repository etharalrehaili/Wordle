package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.khammin.core.presentation.components.ConfettiLayer
import com.khammin.core.presentation.components.PlayerAvatar
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.contract.OpponentProgress

private data class RankedPlayer(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val avatarColor: Long?,
    val avatarEmoji: String?,
    val sessionPoints: Int,
    val isMe: Boolean,
)

@Composable
fun RandomWordGameOverLobby(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    winnerName: String,
    targetWord: String,
    myName: String,
    myAvatarColor: Long?,
    myAvatarEmoji: String?,
    myAvatarUrl: String?,
    myUserId: String,
    roundNumber: Int,
    opponentsProgress: Map<String, OpponentProgress>,
    sessionPoints: Map<String, Int>,
    isHost: Boolean,
    onPlayAgain: () -> Unit,
    onLeave: () -> Unit,
) {
    val accentColor = if (isWin) colors.logoGreen else colors.logoPink

    // Build ranked leaderboard (me + all opponents), sorted by session points descending
    val me = RankedPlayer(
        userId = myUserId,
        name = myName.ifBlank { "You" },
        avatarUrl = myAvatarUrl,
        avatarColor = myAvatarColor,
        avatarEmoji = myAvatarEmoji,
        sessionPoints = sessionPoints[myUserId] ?: 0,
        isMe = true,
    )
    val opponents = opponentsProgress.entries.map { (userId, p) ->
        RankedPlayer(
            userId = userId,
            name = p.name,
            avatarUrl = p.avatarUrl,
            avatarColor = p.avatarColor,
            avatarEmoji = p.avatarEmoji,
            sessionPoints = sessionPoints[userId] ?: 0,
            isMe = false,
        )
    }
    val ranked = (listOf(me) + opponents).sortedByDescending { it.sessionPoints }


    Box(modifier = modifier) {

        // ── Confetti overlay (win only) ───────────────────────────
        if (isWin) {
            ConfettiLayer(modifier = Modifier.matchParentSize())
        }

        LazyColumn(
            modifier = modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        ) {

            // ── Result header ─────────────────────────────────────────────────────
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.lobby_round, roundNumber),
                        color = colors.logoBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = if (isWin) stringResource(R.string.lobby_you_won)
                        else stringResource(R.string.lobby_you_lost),
                        color = accentColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    if (!isWin && winnerName.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.lobby_winner_won, winnerName),
                            color = colors.body.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (targetWord.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.lobby_the_word_was),
                            color = colors.body.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            targetWord.forEach { letter ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .border(
                                            1.dp,
                                            accentColor.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        color = accentColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Ranked leaderboard ────────────────────────────────────────────────
            item {
                Text(
                    text = stringResource(R.string.lobby_leaderboard),
                    color = colors.body.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth(),
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
                            1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "$rank."
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (player.isMe) Modifier.background(
                                        colors.logoBlue.copy(
                                            alpha = 0.06f
                                        )
                                    ) else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = rankLabel,
                                    fontSize = if (rank <= 3) 20.sp else 13.sp,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center,
                                )
                                PlayerAvatarSmall(
                                    name = player.name,
                                    avatarUrl = player.avatarUrl,
                                    avatarColor = player.avatarColor,
                                    avatarEmoji = player.avatarEmoji,
                                )
                                Text(
                                    text = player.name,
                                    color = colors.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Medium,
                                )
                                if (player.isMe) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(colors.logoPink.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.lobby_badge_you_label),
                                            color = colors.logoPink,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Text(
                                text = String.format(Locale.US, stringResource(R.string.lobby_pts), player.sessionPoints),
                                color = colors.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // ── Action ───────────────────────────────────────────────────────────
            item {
                val allPlayersFinished = opponentsProgress.values.all { it.solved || it.failed }
                val canPlayAgain = isWin || allPlayersFinished
                if (isHost) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GameButton(
                            label = stringResource(R.string.lobby_play_again),
                            onClick = onPlayAgain,
                            enabled = canPlayAgain,
                            variant = if (canPlayAgain) GameButtonVariant.Primary else GameButtonVariant.Muted,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!canPlayAgain) {
                            Text(
                                text = stringResource(R.string.lobby_waiting_for_players),
                                color = colors.body.copy(alpha = 0.45f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.lobby_waiting_next_round),
                            color = colors.body.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            item {
                GameButton(
                    label    = stringResource(R.string.lobby_leave),
                    onClick  = onLeave,
                    variant  = GameButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlayerAvatarSmall(
    name: String,
    avatarUrl: String?,
    avatarColor: Long?,
    avatarEmoji: String?,
) {
    when {
        avatarUrl != null -> PlayerAvatar(
            name      = name,
            avatarUrl = avatarUrl,
            modifier  = Modifier.size(32.dp).clip(CircleShape),
            fontSize  = 13.sp,
        )
        avatarColor != null && avatarEmoji != null -> {
            val c = Color(avatarColor)
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(c.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) { Text(avatarEmoji, fontSize = 15.sp) }
        }
        avatarColor != null -> {
            val c = Color(avatarColor)
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(c.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) { Text(name.take(1).uppercase(), color = c, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
        else -> Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.logoBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = name.take(1).uppercase(),
                color      = colors.logoBlue,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}