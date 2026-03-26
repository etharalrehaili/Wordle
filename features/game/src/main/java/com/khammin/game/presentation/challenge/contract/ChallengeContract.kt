package com.khammin.game.presentation.challenge.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.WORD_LENGTH
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.game.presentation.game.contract.Tile

sealed interface ChallengeIntent: UiIntent {
    data class LoadWords(val language: String) : ChallengeIntent
    data class EnterLetter(val letter: Char)   : ChallengeIntent
    object DeleteLetter                        : ChallengeIntent
    object SubmitGuess                         : ChallengeIntent
}

sealed interface ChallengeEffect: UiEffect {
    object InvalidWord : ChallengeEffect
    data object NotInWordList : ChallengeEffect
    object RowShake    : ChallengeEffect
    data class ShowGameDialog(val isWin: Boolean, val targetWord: String) : ChallengeEffect
}

data class ChallengeUiState(
    val isLoading: Boolean        = true,
    val error: String?            = null,
    val wordList: List<String>    = emptyList(),
    val targetWord: String        = "",
    val wordLength: Int           = WORD_LENGTH,
    val board: List<List<Tile>>   = List(MAX_GUESSES) { List(WORD_LENGTH) { Tile() } },
    val keyboardStates: Map<Char, TileState> = emptyMap(),
    val currentRow: Int           = 0,
    val currentCol: Int           = 0,
    val isGameOver: Boolean       = false,
    val language: String = "",
) : UiState

sealed interface ChallengeDialogState {
    object None                                           : ChallengeDialogState
    object Info                                           : ChallengeDialogState
    data class Result(val isWin: Boolean, val word: String) : ChallengeDialogState
}