package com.wordle.game.presentation.game.vm

import androidx.lifecycle.viewModelScope
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.util.Resource
import com.wordle.game.domain.usecases.game.GetWordsUseCase
import com.wordle.game.presentation.game.contract.GameEffect
import com.wordle.game.presentation.game.contract.GameIntent
import com.wordle.game.presentation.game.contract.GameUiState
import com.wordle.game.presentation.game.contract.Tile
import com.wordle.game.presentation.game.contract.TileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase
) : BaseMviViewModel<GameIntent, GameUiState, GameEffect>(
    initialState = GameUiState()
) {

    override fun onEvent(intent: GameIntent) {
        when (intent) {
            is GameIntent.LoadWords    -> loadWords(intent.language, intent.wordLength)
            is GameIntent.EnterLetter  -> enterLetter(intent.letter)
            is GameIntent.DeleteLetter -> deleteLetter()
            is GameIntent.SubmitGuess  -> submitGuess()
            is GameIntent.RestartGame  -> restartGame()
        }
    }

    private fun loadWords(language: String, wordLength: Int) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = getWordsUseCase(language, wordLength)) {
                is Resource.Success -> {
                    val words = result.data
                    setState {
                        copy(
                            isLoading  = false,
                            wordLength = wordLength,
                            wordList   = words,
                            targetWord = words.random().uppercase(),
                            board      = List(MAX_GUESSES) { List(wordLength) { Tile() } }
                        )
                    }
                }
                is Resource.Error   -> setState { copy(isLoading = false, error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun enterLetter(letter: Char) {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.targetWord.isEmpty()) return
        if (state.currentCol >= state.wordLength) return

        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = state.currentCol,
            tile = Tile(letter = letter.uppercaseChar(), state = TileState.FILLED)
        )
        setState { copy(board = newBoard, currentCol = currentCol + 1) }

        if (state.currentCol == state.wordLength - 1) submitGuess()
    }

    private fun deleteLetter() {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.currentCol <= 0) return

        val colToDelete = state.currentCol - 1
        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = colToDelete,
            tile = Tile()
        )
        setState { copy(board = newBoard, currentCol = colToDelete) }
    }

    private fun submitGuess() {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.currentCol < state.wordLength) {
            sendEffect { GameEffect.InvalidWord }
            sendEffect { GameEffect.RowShake }
            return
        }

        val guess = state.board[state.currentRow].map { it.letter }.joinToString("")

        if (!state.wordList.any { it.equals(guess, ignoreCase = true) }) {
            sendEffect { GameEffect.InvalidWord }
            sendEffect { GameEffect.RowShake }
            return
        }

        val evaluatedRow = evaluateGuess(guess, state.targetWord)
        val newBoard = state.board.toMutableList()
            .also { it[state.currentRow] = evaluatedRow }.toList()
        val newKeyboardStates = state.keyboardStates.mergeWith(evaluatedRow)

        val isWin  = evaluatedRow.all { it.state == TileState.CORRECT }
        val isLast = state.currentRow == MAX_GUESSES - 1

        setState {
            copy(
                board          = newBoard,
                keyboardStates = newKeyboardStates,
                currentRow     = if (isWin || isLast) currentRow else currentRow + 1,
                currentCol     = 0,
                isGameOver     = isWin || isLast
            )
        }

        if (isWin || isLast) {
            sendEffect { GameEffect.ShowGameDialog(isWin = isWin, targetWord = state.targetWord) }
        }
    }

    private fun restartGame() {
        val state = uiState.value
        setState {
            GameUiState(
                wordLength = state.wordLength,
                wordList   = state.wordList,
                targetWord = state.wordList.randomOrNull()?.uppercase() ?: "",
                board      = List(MAX_GUESSES) { List(state.wordLength) { Tile() } }
            )
        }
    }

    private fun evaluateGuess(guess: String, target: String): List<Tile> {
        val wordLength      = uiState.value.wordLength
        val result          = Array(wordLength) { TileState.WRONG }
        val targetArr       = target.toCharArray()
        val guessArr        = guess.toCharArray()
        val remainingTarget = targetArr.toMutableList()

        for (i in guessArr.indices) {
            if (guessArr[i] == targetArr[i]) {
                result[i]          = TileState.CORRECT
                remainingTarget[i] = '\u0000'
            }
        }
        for (i in guessArr.indices) {
            if (result[i] == TileState.CORRECT) continue
            val idx = remainingTarget.indexOf(guessArr[i])
            if (idx != -1) {
                result[i]          = TileState.MISPLACED
                remainingTarget[idx] = '\u0000'
            }
        }
        return guessArr.mapIndexed { i, ch -> Tile(letter = ch, state = result[i]) }
    }

    private fun List<List<Tile>>.updateTile(row: Int, col: Int, tile: Tile): List<List<Tile>> =
        mapIndexed { r, rowList ->
            if (r != row) rowList
            else rowList.mapIndexed { c, t -> if (c == col) tile else t }
        }

    private fun Map<Char, TileState>.mergeWith(row: List<Tile>): Map<Char, TileState> {
        val priority = mapOf(
            TileState.CORRECT   to 4,
            TileState.MISPLACED to 3,
            TileState.WRONG     to 2,
            TileState.FILLED    to 1,
            TileState.EMPTY     to 0
        )
        val merged = toMutableMap()
        for (tile in row) {
            val current = merged[tile.letter]
            if (current == null || priority.getValue(tile.state) > priority.getValue(current)) {
                merged[tile.letter] = tile.state
            }
        }
        return merged
    }
}