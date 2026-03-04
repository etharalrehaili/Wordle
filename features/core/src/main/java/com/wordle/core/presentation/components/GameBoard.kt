package com.wordle.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.theme.LocalWordleColors

const val WORD_LENGTH = 4
const val MAX_GUESSES = 6

/**
 * Represents a single revealed/in-progress row on the board.
 */
data class GuessRow(
    val letters: List<Char?> = List(WORD_LENGTH) { null },
    val types: List<Types>   = List(WORD_LENGTH) { Types.DEFAULT }
)

@Composable
fun GameBoard(
    guesses: List<GuessRow> = List(MAX_GUESSES) { GuessRow() },
    currentRow: Int = 0,
    currentCol: Int = 0,
    onKey: (Char) -> Unit = {},
    onEnter: () -> Unit = {},
    onBackspace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val colors = LocalWordleColors.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(currentRow, currentCol) {
        focusRequester.requestFocus()
    }

    Box(modifier = modifier.fillMaxWidth()) {

        var textFieldValue by remember { mutableStateOf("") }

        TextField(
            value = textFieldValue,
            onValueChange = { newText ->

                if (newText.isNotEmpty()) {
                    newText.forEach { char ->
                        if (char.isLetter()) {
                            val finalChar =
                                if (char in 'a'..'z') char.uppercaseChar()
                                else char

                            onKey(finalChar)
                        }
                    }
                }

                textFieldValue = "" // clear after handling
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Backspace, Key.Delete -> {
                                onBackspace()
                                true
                            }
                            Key.Enter -> {
                                onEnter()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .height(0.dp), // hide it
            singleLine = true
        )

        // ── Board grid ────────────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(16.dp)
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
        ) {
            repeat(MAX_GUESSES) { rowIndex ->
                val guess = guesses.getOrNull(rowIndex) ?: GuessRow()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    repeat(WORD_LENGTH) { colIndex ->
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