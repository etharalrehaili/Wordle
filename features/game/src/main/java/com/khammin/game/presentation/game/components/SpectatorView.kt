package com.khammin.game.presentation.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.contract.OpponentProgress

@Composable
fun SpectatorView(
    modifier: Modifier = Modifier,
    word: String,
    wordLength: Int,
    opponentsProgress: Map<String, OpponentProgress>,
    roundNumber: Int = 1,
    playAgainVoteCount: Int = 0,
    playAgainVotes: List<String> = emptyList(),
    totalGuests: Int = 0,
    onPlayAgain: () -> Unit = {}
) {
    val roundOver = opponentsProgress.isNotEmpty() &&
            (opponentsProgress.values.any { it.solved } || opponentsProgress.values.all { it.failed })

    Column(
        modifier            = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text          = stringResource(R.string.spectator_round, roundNumber),
            color         = colors.logoBlue,
            fontSize      = 13.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )

        if (word.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                word.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.5.dp, colors.logoBlue, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = letter.toString(),
                            color      = colors.title,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }

        if (roundOver && totalGuests > 0) {
            VotePanel(
                playAgainVotes    = playAgainVotes,
                totalGuests       = totalGuests,
                opponentsProgress = opponentsProgress,
            )
        }

        if (opponentsProgress.isEmpty()) {
            Text(
                text      = stringResource(R.string.spectator_waiting),
                color     = colors.body.copy(alpha = 0.65f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )
        } else {
            val sortedProgress = opponentsProgress.values.sortedWith(
                compareByDescending<OpponentProgress> { it.solved }
                    .thenBy { if (it.solved) it.guessCount else Int.MAX_VALUE }
                    .thenByDescending { it.failed }
            )

            LazyColumn(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sortedProgress) { progress ->
                    val points = if (progress.solved) when (progress.guessCount) {
                        1    -> 100
                        2    -> 80
                        3    -> 60
                        4    -> 40
                        5    -> 20
                        else -> 10
                    } else 0

                    val statusText = when {
                        progress.solved          -> stringResource(R.string.spectator_solved, progress.guessCount, points)
                        progress.failed          -> stringResource(R.string.spectator_failed)
                        progress.guessCount == 0 -> stringResource(R.string.spectator_waiting_guess)
                        else                     -> stringResource(R.string.spectator_guess_progress, progress.guessCount, MAX_GUESSES)
                    }

                    val statusColor = when {
                        progress.solved -> colors.logoGreen
                        progress.failed -> colors.logoPink
                        else            -> colors.body.copy(alpha = 0.55f)
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        GuestCard(
                            name        = progress.name,
                            avatarUrl   = progress.avatarUrl,
                            avatarColor = progress.avatarColor,
                            avatarEmoji = progress.avatarEmoji,
                            guesses     = progress.guessRows,
                            wordLength  = wordLength,
                        )
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = statusText,
                                    color      = statusColor,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (progress.totalPoints > 0) {
                                Text(
                                    text       = stringResource(R.string.spectator_total_points, progress.totalPoints),
                                    color      = colors.body.copy(alpha = 0.5f),
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }

        GameButton(
            label    = stringResource(R.string.lobby_play_again),
            onClick  = onPlayAgain,
            variant  = if (roundOver) GameButtonVariant.Primary else GameButtonVariant.Muted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VotePanel(
    playAgainVotes: List<String>,
    totalGuests: Int,
    opponentsProgress: Map<String, OpponentProgress>,
) {
    val fraction = if (totalGuests > 0) playAgainVotes.size.toFloat() / totalGuests else 0f
    val animatedFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(500),
        label         = "voteProgress",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text       = stringResource(R.string.spectator_play_again_question),
                color      = colors.title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text       = stringResource(R.string.spectator_vote_count, playAgainVotes.size, totalGuests),
                color      = colors.logoBlue,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.border),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.logoBlue),
            )
        }

        if (playAgainVotes.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                playAgainVotes.forEach { voterId ->
                    val voter = opponentsProgress[voterId]
                    key(voterId) {
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter   = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness    = Spring.StiffnessMedium,
                                )
                            ) + fadeIn(tween(150)),
                        ) {
                            VoterBubble(
                                name        = voter?.name ?: "?",
                                avatarColor = voter?.avatarColor,
                                avatarEmoji = voter?.avatarEmoji,
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text     = stringResource(R.string.spectator_no_votes),
                color    = colors.body.copy(alpha = 0.4f),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
internal fun VoterBubble(
    name: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    size: Int = 32,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(2.dp, colors.background, CircleShape)
            .background(
                if (avatarColor != null) Color(avatarColor).copy(alpha = 0.25f)
                else colors.logoBlue.copy(alpha = 0.20f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarColor != null && avatarEmoji != null) {
            Text(text = avatarEmoji, fontSize = (size * 0.44f).sp)
        } else {
            val tint = if (avatarColor != null) Color(avatarColor) else colors.logoBlue
            Text(
                text       = name.take(1).uppercase(),
                color      = tint,
                fontSize   = (size * 0.38f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private val previewProgressSolved = OpponentProgress(
    name        = "Alice",
    avatarColor = 0xFF00BCD4,
    avatarEmoji = "🐱",
    solved      = true,
    failed      = false,
    guessCount  = 3,
    totalPoints = 60,
    guessRows   = emptyList(),
)

private val previewProgressFailed = OpponentProgress(
    name        = "Bob",
    avatarColor = 0xFFE91E8C,
    avatarEmoji = "🐶",
    solved      = false,
    failed      = true,
    guessCount  = 6,
    totalPoints = 0,
    guessRows   = emptyList(),
)

private val previewProgressPlaying = OpponentProgress(
    name        = "Charlie",
    avatarColor = 0xFF9C27B0,
    avatarEmoji = "🦊",
    solved      = false,
    failed      = false,
    guessCount  = 2,
    totalPoints = 120,
    guessRows   = emptyList(),
)

@GameLightBackgroundPreview
@Composable
fun SpectatorViewActivePreview() {
    SpectatorView(
        word              = "SWIFT",
        wordLength        = 5,
        opponentsProgress = mapOf(
            "1" to previewProgressSolved,
            "2" to previewProgressFailed,
            "3" to previewProgressPlaying,
        ),
        roundNumber    = 2,
        playAgainVotes = listOf("1"),
        totalGuests    = 3,
    )
}

@GameLightBackgroundPreview
@Composable
fun SpectatorViewWaitingPreview() {
    SpectatorView(
        word              = "HELLO",
        wordLength        = 5,
        opponentsProgress = emptyMap(),
        roundNumber       = 1,
    )
}