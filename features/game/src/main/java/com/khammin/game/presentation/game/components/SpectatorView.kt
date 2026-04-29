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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Locale

@Composable
fun SpectatorView(
    modifier: Modifier = Modifier,
    word: String,
    wordLength: Int,
    opponentsProgress: Map<String, OpponentProgress>,
    roundNumber: Int = 1,
    onPlayAgain: () -> Unit = {},
) {
    val roundOver = opponentsProgress.isNotEmpty() &&
            (opponentsProgress.values.any { it.solved } || opponentsProgress.values.all { it.failed })

    Column(
        modifier            = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text          = stringResource(R.string.spectator_round, String.format(Locale.US, "%d", roundNumber)),
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
                        progress.solved -> stringResource(R.string.spectator_solved, String.format(
                            Locale.US, "%d", points))
                        progress.failed          -> stringResource(R.string.spectator_failed)
                        progress.guessCount == 0 -> stringResource(R.string.spectator_waiting_guess)
                        else                     -> stringResource(R.string.spectator_guess_progress,
                            String.format(Locale.US, "%d", progress.guessCount),
                            String.format(Locale.US, "%d", MAX_GUESSES))
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
                            val runningTotal = progress.totalPoints + points
                            if (runningTotal > 0) {
                                Text(
                                    text = stringResource(R.string.spectator_total_points, String.format(java.util.Locale.US, "%d", runningTotal)),
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

        if (roundOver) {
            GameButton(
                label    = stringResource(R.string.lobby_play_again),
                onClick  = onPlayAgain,
                variant  = GameButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
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
        roundNumber = 2,
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
