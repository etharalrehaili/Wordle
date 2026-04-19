package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.khammin.core.R as CoreRes
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.multiplayer.GuestCard
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.OpponentProgress

@Composable
fun SpectatorView(
    modifier: Modifier = Modifier,
    word: String,
    wordLength: Int,
    opponentsProgress: Map<String, OpponentProgress>,
    roundNumber: Int = 1,
    playAgainVoteCount: Int = 0,
    totalGuests: Int = 0,
    onPlayAgain: () -> Unit = {}
) {
    val roundOver = opponentsProgress.isNotEmpty() &&
            (opponentsProgress.values.any { it.solved } || opponentsProgress.values.all { it.failed })
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Round label
        Text(
            text       = "Round $roundNumber",
            color      = colors.buttonTeal,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )

        // Word tiles so the host always sees what word they set
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
                            .border(1.5.dp, colors.buttonTeal, RoundedCornerShape(8.dp)),
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

        if (playAgainVoteCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.buttonTeal.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "👋  Votes for play again: $playAgainVoteCount / $totalGuests",
                    color      = colors.buttonTeal,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (opponentsProgress.isEmpty()) {
            Text(
                text      = stringResource(CoreRes.string.spectator_waiting),
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
                        progress.solved -> "Solved in ${progress.guessCount} ·  $points pts"
                        progress.failed -> "Failed"
                        progress.guessCount == 0 -> "Waiting…"
                        else -> "${progress.guessCount}/${MAX_GUESSES}"
                    }
                    val statusColor = when {
                        progress.solved -> colors.buttonTeal
                        progress.failed -> colors.buttonPink
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
                                    text       = "Total: ${progress.totalPoints} pts",
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

        Button(
            onClick  = onPlayAgain,
            enabled  = roundOver,
            modifier = Modifier
                .wrapContentWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = colors.buttonTeal,
                disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
            ),
        ) {
            Text(
                text       = stringResource(CoreRes.string.result_play_again),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.background,
            )
        }
    }
}

// --- Preview Data ---
private val previewProgressSolved = OpponentProgress(
    name        = "Alice / أليس",
    avatarColor = 0xFF00BCD4,
    avatarEmoji = "🐱",
    solved      = true,
    failed      = false,
    guessCount  = 3,
    totalPoints = 60,
    guessRows   = emptyList(),
)

private val previewProgressFailed = OpponentProgress(
    name        = "Bob / بوب",
    avatarColor = 0xFFE91E8C,
    avatarEmoji = "🐶",
    solved      = false,
    failed      = true,
    guessCount  = 6,
    totalPoints = 0,
    guessRows   = emptyList(),
)

private val previewProgressPlaying = OpponentProgress(
    name        = "Charlie / تشارلي",
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
        word               = "SWIFT",
        wordLength         = 5,
        opponentsProgress  = mapOf(
            "1" to previewProgressSolved,
            "2" to previewProgressFailed,
            "3" to previewProgressPlaying,
        ),
        roundNumber        = 2,
        playAgainVoteCount = 1,
        totalGuests        = 3,
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