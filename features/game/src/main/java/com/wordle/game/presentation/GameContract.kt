package com.wordle.game.presentation

import com.wordle.core.mvi.UiEffect
import com.wordle.core.mvi.UiIntent
import com.wordle.core.mvi.UiState
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.presentation.components.WORD_LENGTH

// ─── Tile State ──────────────────────────────────────────────────────────────

enum class TileState {
    EMPTY,      // not yet filled
    FILLED,     // filled but not yet submitted
    CORRECT,    // right letter, right position  (green)
    MISPLACED,  // right letter, wrong position  (yellow)
    WRONG       // letter not in word             (gray)
}

data class Tile(
    val letter: Char = ' ',
    val state: TileState = TileState.EMPTY
)

// ─── UI State ────────────────────────────────────────────────────────────────

data class GameUiState(
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val board: List<List<Tile>> = List(MAX_GUESSES) { List(WORD_LENGTH) { Tile() } },
    // tracks the best-known state for each letter (for keyboard coloring)
    val keyboardStates: Map<Char, TileState> = emptyMap(),
    val wordList: List<String> = emptyList(),
    val targetWord: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGameOver: Boolean = false,
) : UiState

// ─── Effects ─────────────────────────────────────────────────────────────────

sealed interface GameEffect : UiEffect {
    data class ShowGameDialog(val isWin: Boolean, val targetWord: String) : GameEffect
    data object InvalidWord : GameEffect        // word not long enough / not in list
    data object RowShake : GameEffect           // trigger shake animation on current row
}

// ─── Intents ─────────────────────────────────────────────────────────────────

sealed class GameIntent : UiIntent {
    data class LoadWords(val language: String) : GameIntent()
    data class EnterLetter(val letter: Char) : GameIntent()
    data object DeleteLetter : GameIntent()
    data object SubmitGuess : GameIntent()
    data object RestartGame : GameIntent()
}