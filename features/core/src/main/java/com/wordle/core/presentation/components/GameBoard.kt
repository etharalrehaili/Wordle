package com.wordle.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.theme.LocalWordleColors

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
    android.util.Log.d("GameBoard", "wordLength=$wordLength, guesses[0].letters.size=${guesses.firstOrNull()?.letters?.size}")

    val colors = LocalWordleColors.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(16.dp)
    ) {
        repeat(MAX_GUESSES) { rowIndex ->
            val guess = guesses.getOrNull(rowIndex) ?: GuessRow()
            val colCount = if (guess.letters.isEmpty()) wordLength else guess.letters.size  // ← fix this
            android.util.Log.d("GameBoard", "row=$rowIndex colCount=$colCount (letters.size=${guess.letters.size})")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
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