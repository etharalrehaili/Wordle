package com.khammin.game.presentation.game.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.presentation.components.enums.Types

data class GameUiState(
    val wordLength: Int = 4,
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val board: List<List<Tile>> = List(MAX_GUESSES) { List(wordLength) { Tile() } },
    val keyboardStates: Map<Char, TileState> = emptyMap(),
    val wordList: List<String> = emptyList(),
    val targetWord: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGameOver: Boolean = false,
    val hintsUsed: Int = 0,
    val maxHints: Int = 3,
    val hasUsedSecondChance: Boolean = false
) : UiState

sealed interface GameEffect : UiEffect {
    data class ShowGameDialog(val isWin: Boolean, val targetWord: String) : GameEffect
    data object InvalidWord : GameEffect
    data object NotInWordList : GameEffect
    data object RowShake : GameEffect
}

sealed class GameIntent : UiIntent {
    data class LoadWords(val language: String, val wordLength: Int) : GameIntent()
    data class EnterLetter(val letter: Char) : GameIntent()
    data object DeleteLetter : GameIntent()
    data object SubmitGuess : GameIntent()
    data object RestartGame : GameIntent()
    data object UseHint : GameIntent()
    data object SecondChance : GameIntent()
}

sealed interface GameDialogState {
    data object None : GameDialogState
    data object Info : GameDialogState
    data class Result(val isWin: Boolean, val word: String) : GameDialogState
}

fun TileState.toTypes(): Types = when (this) {
    TileState.CORRECT   -> Types.CORRECT
    TileState.MISPLACED -> Types.PRESENT
    TileState.WRONG     -> Types.ABSENT
    TileState.SIMILAR   -> Types.SIMILAR
    TileState.FILLED,
    TileState.EMPTY     -> Types.DEFAULT
}

data class Tile(
    val letter: Char = ' ',
    val state: TileState = TileState.EMPTY
)
