package com.khammin.core.presentation.components.multiplayer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.GuessRow
import com.khammin.core.presentation.components.PlayerAvatar
import com.khammin.core.presentation.components.WORD_LENGTH
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.WordleTheme

@Composable
fun GuestCard(
    modifier: Modifier = Modifier,
    name: String = "Guest",
    avatarUrl: String? = null,
    isLoading: Boolean = false,
    guesses: List<GuessRow> = listOf(
        GuessRow(listOf('S','L','A','T'), listOf(Types.CORRECT, Types.ABSENT,  Types.ABSENT,  Types.PRESENT)),
        GuessRow(listOf('S','O','U','N'), listOf(Types.CORRECT, Types.CORRECT, Types.ABSENT,  Types.ABSENT)),
        GuessRow(listOf('S','T','O','R'), listOf(Types.CORRECT, Types.PRESENT, Types.ABSENT,  Types.CORRECT)),
        GuessRow(listOf('S','T','A','R'), listOf(Types.CORRECT, Types.CORRECT, Types.PRESENT, Types.CORRECT)),
        GuessRow(), GuessRow()
    ),
    currentRow: Int = 0,
    currentCol: Int = 0,
    wordLength: Int = WORD_LENGTH
) {
    val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue    = 0.3f,
        targetValue     = 0.7f,
        animationSpec   = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.key)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(shimmerAlpha)
                            .background(colors.surface)
                    )
                } else {
                    PlayerAvatar(
                        name      = name,
                        avatarUrl = avatarUrl,
                        modifier  = Modifier.fillMaxSize(),
                        fontSize  = 24.sp
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(12.dp)
                        .alpha(shimmerAlpha)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                )
            } else {
                Text(
                    text          = name,
                    color         = colors.title,
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                )
            }
        }

        MiniBoard(
            guesses    = guesses,
            wordLength = wordLength,
            modifier   = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun MiniBoard(
    guesses: List<GuessRow>,
    wordLength: Int,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        repeat(MAX_GUESSES) { rowIndex ->
            val guess = guesses.getOrNull(rowIndex) ?: GuessRow()
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(wordLength) { colIndex ->
                    val type   = guess.types.getOrNull(colIndex) ?: Types.DEFAULT
                    val filled = guess.letters.getOrNull(colIndex) != null

                    val cellColor = when (type) {
                        Types.CORRECT -> colors.correct
                        Types.PRESENT -> colors.present
                        Types.ABSENT  -> colors.absent
                        Types.SIMILAR  -> Color.Transparent // handled by KeyContainer's diagonal split
                        Types.DEFAULT -> if (filled) colors.borderActive else colors.border
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(cellColor)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GuestCard – in progress")
@Composable
private fun PreviewGuestCardInProgress() {
    GuestCard()
}

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GuestCard – empty")
@Composable
private fun PreviewGuestCardEmpty() {
    WordleTheme {
        GuestCard(guesses = List(MAX_GUESSES) { GuessRow() })
    }
}