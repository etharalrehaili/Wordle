package com.wordle.game.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.presentation.components.WORD_LENGTH
import com.wordle.core.util.Resource
import com.wordle.game.domain.usecases.GetWordsUseCase
import com.wordle.game.domain.usecases.LoadTodayChallengeUseCase
import com.wordle.game.domain.usecases.SaveChallengeStateUseCase
import com.wordle.game.presentation.contract.ChallengeEffect
import com.wordle.game.presentation.contract.ChallengeIntent
import com.wordle.game.presentation.contract.ChallengeUiState
import com.wordle.game.presentation.contract.Tile
import com.wordle.game.presentation.contract.TileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val loadTodayChallengeUseCase: LoadTodayChallengeUseCase,
    private val saveChallengeStateUseCase: SaveChallengeStateUseCase,
) : BaseMviViewModel<ChallengeIntent, ChallengeUiState, ChallengeEffect>(
    initialState = ChallengeUiState()
) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEvent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.LoadWords   -> loadWords(intent.language)
            is ChallengeIntent.EnterLetter -> enterLetter(intent.letter)
            is ChallengeIntent.DeleteLetter -> deleteLetter()
            is ChallengeIntent.SubmitGuess -> submitGuess()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadWords(language: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = getWordsUseCase(language)) {
                is Resource.Success -> {
                    val words = result.data
                    val saved = loadTodayChallengeUseCase()

                    if (saved != null) {
                        setState {
                            copy(
                                isLoading      = false,
                                wordList       = words,
                                targetWord     = saved.targetWord,
                                board          = saved.board,
                                keyboardStates = saved.keyboardStates,
                                currentRow     = saved.currentRow,
                                currentCol     = saved.currentCol,
                                isGameOver     = saved.isGameOver,
                            )
                        }
                        if (saved.isGameOver) {
                            sendEffect {
                                ChallengeEffect.ShowGameDialog(
                                    isWin      = saved.isWin,
                                    targetWord = saved.targetWord
                                )
                            }
                        }
                    } else {
                        setState {
                            copy(
                                isLoading  = false,
                                wordList   = words,
                                targetWord = words.dailyWord().uppercase()
                            )
                        }
                    }
                }
                is Resource.Error   -> setState { copy(isLoading = false, error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun List<String>.dailyWord(): String {
        if (isEmpty()) return ""
        val dayIndex = LocalDate.now().toEpochDay()
        return this[(dayIndex % size).toInt().coerceAtLeast(0)]
    }

    private fun enterLetter(letter: Char) {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.currentCol >= WORD_LENGTH) return

        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = state.currentCol,
            tile = Tile(letter = letter.uppercaseChar(), state = TileState.FILLED)
        )
        setState { copy(board = newBoard, currentCol = currentCol + 1) }

        if (state.currentCol == WORD_LENGTH - 1) submitGuess()
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
        if (state.currentCol < WORD_LENGTH) {
            sendEffect { ChallengeEffect.InvalidWord }
            sendEffect { ChallengeEffect.RowShake }
            return
        }

        val guess = state.board[state.currentRow].map { it.letter }.joinToString("")
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

        viewModelScope.launch {
            val s = uiState.value
            saveChallengeStateUseCase(
                targetWord     = s.targetWord,
                board          = s.board,
                keyboardStates = s.keyboardStates,
                currentRow     = s.currentRow,
                currentCol     = s.currentCol,
                isGameOver     = s.isGameOver,
                isWin          = isWin,
            )
        }

        if (isWin || isLast) {
            sendEffect { ChallengeEffect.ShowGameDialog(isWin = isWin, targetWord = state.targetWord) }
        }
    }

    private fun evaluateGuess(guess: String, target: String): List<Tile> {
        val result    = Array(WORD_LENGTH) { TileState.WRONG }
        val targetArr = target.toCharArray()
        val guessArr  = guess.toCharArray()

        val remainingTarget = targetArr.toMutableList()
        for (i in guessArr.indices) {
            if (guessArr[i] == targetArr[i]) {
                result[i] = TileState.CORRECT
                remainingTarget[i] = '\u0000'
            }
        }
        for (i in guessArr.indices) {
            if (result[i] == TileState.CORRECT) continue
            val idx = remainingTarget.indexOf(guessArr[i])
            if (idx != -1) {
                result[i] = TileState.MISPLACED
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