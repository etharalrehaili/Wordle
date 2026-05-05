package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.theme.GameDesignTheme.colors

const val WORD_LENGTH = 4
const val MAX_GUESSES = 6

data class GuessRow(
    val letters: List<Char?> = emptyList(),
    val types: List<Types>   = emptyList()
)

@Composable
fun GameBoard(
    modifier: Modifier = Modifier,
    guesses: List<GuessRow> = List(MAX_GUESSES) { GuessRow() },
    currentRow: Int = 0,
    currentCol: Int = 0,
    wordLength: Int = WORD_LENGTH
) {
    val clampedWordLength = wordLength.coerceIn(4, 6)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(2.dp)
    ) {
        val rowCount   = guesses.size.coerceAtLeast(1)
        val tileFromH  = (maxHeight - 2.dp * (rowCount - 1)) / rowCount
        val tileFromW  = (maxWidth  - 2.dp * (clampedWordLength - 1)) / clampedWordLength
        val tileSize: Dp = minOf(tileFromH, tileFromW, 80.dp)
        val gap = (maxHeight * 0.01f).coerceIn(2.dp, 6.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
        ) {
            guesses.forEachIndexed { rowIndex, guess ->
                val colCount = clampedWordLength

                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(colCount) { colIndex ->
                        val letter   = guess.letters.getOrNull(colIndex)
                        val type     = guess.types.getOrNull(colIndex) ?: Types.DEFAULT
                        val isActive = rowIndex == currentRow && colIndex == currentCol

                        Square(
                            content  = if (letter != null) SquareContent.Letter(letter)
                            else SquareContent.Empty,
                            type     = type,
                            isActive = isActive,
                            isKey    = false,
                            modifier = Modifier.size(tileSize)
                        )
                    }
                }
            }
        }
    }
}

fun PlayerState.toGuessRows(wordLength: Int): List<GuessRow> {
    val rows = guesses.mapIndexed { i, guessStr ->
        val letters = guessStr.split(",").filter { it.isNotEmpty() }.map { it[0] }
        val typeList = types.getOrNull(i)
            ?.split(",")
            ?.map { t ->
                when (t) {
                    "CORRECT"  -> Types.CORRECT
                    "SIMILAR"  -> Types.SIMILAR
                    "PRESENT"  -> Types.PRESENT
                    "ABSENT"   -> Types.ABSENT
                    else       -> Types.DEFAULT
                }
            } ?: emptyList()
        GuessRow(letters, typeList)
    }
    return rows + List(MAX_GUESSES - rows.size) { GuessRow() }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GameBoard")
@Composable
private fun PreviewGameBoard() {
    GameBoard(
        guesses = listOf(
            GuessRow(listOf('S','L','A','T','E'), listOf(Types.CORRECT, Types.ABSENT, Types.ABSENT, Types.PRESENT, Types.ABSENT)),
            GuessRow(listOf('S','O','U','N','D'), listOf(Types.CORRECT, Types.CORRECT, Types.ABSENT, Types.ABSENT, Types.ABSENT)),
            GuessRow(listOf('S','T','O','R',null), List(WORD_LENGTH) { Types.DEFAULT }),
            GuessRow(), GuessRow(), GuessRow(),
        ),
        currentRow = 2,
        currentCol = 3
    )
}
