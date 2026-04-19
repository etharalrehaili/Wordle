package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.LocalWordleColors

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

    val listState = rememberLazyListState()
    val density   = LocalDensity.current

    // Captures the natural height of MAX_GUESSES rows on the very first layout,
    // then stays fixed — so adding extra rows never resizes the board.
    var lockedHeightPx by remember { mutableIntStateOf(0) }

    // Scroll to the newest row whenever one is added (e.g. second chance)
    LaunchedEffect(guesses.size) {
        if (guesses.isNotEmpty()) {
            listState.animateScrollToItem(guesses.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            // Lock to the captured height once we have it
            .then(
                if (lockedHeightPx > 0)
                    Modifier.height(with(density) { lockedHeightPx.toDp() })
                else
                    Modifier
            )
            .background(colors.background)
            .padding(4.dp)
            // Measure the board height exactly once (on the initial MAX_GUESSES layout)
            .onGloballyPositioned { coords ->
                if (lockedHeightPx == 0 && coords.size.height > 0) {
                    lockedHeightPx = coords.size.height
                }
            }
    ) {
        itemsIndexed(guesses) { rowIndex, guess ->
            val colCount = if (guess.letters.isEmpty()) wordLength else guess.letters.size

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(colCount) { colIndex ->
                    val letter = guess.letters.getOrNull(colIndex)
                    val type = guess.types.getOrNull(colIndex) ?: Types.DEFAULT
                    val isActive = rowIndex == currentRow && colIndex == currentCol

                    Square(
                        content = if (letter != null) SquareContent.Letter(letter)
                        else SquareContent.Empty,
                        type = type,
                        isActive = isActive,
                        isKey = false,
                        modifier = Modifier.weight(1f)
                    )
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