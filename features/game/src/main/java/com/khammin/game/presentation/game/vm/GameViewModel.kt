package com.khammin.game.presentation.game.vm

import androidx.lifecycle.viewModelScope
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.util.Resource
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.RecordWinUseCase
import com.khammin.game.domain.usecases.game.ValidateWordUseCase
import com.khammin.game.presentation.game.contract.GameEffect
import com.khammin.game.presentation.game.contract.GameIntent
import com.khammin.game.presentation.game.contract.GameUiState
import com.khammin.game.presentation.game.contract.Tile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val recordWinUseCase: RecordWinUseCase,
    private val validateWordUseCase: ValidateWordUseCase,
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
            is GameIntent.UseHint      -> useHint()
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
                            language   = language,
                            wordLength = wordLength,
                            wordList   = words,
                            targetWord = words.random().normalizeForWordle(),
                            board      = List(MAX_GUESSES) { List(wordLength) { Tile() } }
                        )
                    }
                }
                is Resource.Error -> {
                    setState { copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun enterLetter(letter: Char) {
        val state = uiState.value
        android.util.Log.d("GameVM", "enterLetter: $letter, isGameOver=${state.isGameOver}, isValidating=${state.isValidating}, targetWord=${state.targetWord}, currentCol=${state.currentCol}, wordLength=${state.wordLength}")
        if (state.isGameOver || state.isValidating) return
        if (state.targetWord.isEmpty()) return
        if (state.currentCol >= state.wordLength) return

        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = state.currentCol,
            tile = Tile(letter = letter.uppercaseChar(), state = TileState.FILLED)
        )
        val newCol = state.currentCol + 1
        setState { copy(board = newBoard, currentCol = newCol) }

        if (newCol == state.wordLength) submitGuess()
    }

    private fun deleteLetter() {
        val state = uiState.value
        if (state.isGameOver || state.isValidating) return
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
        if (state.isGameOver || state.isValidating) return
        if (state.currentCol < state.wordLength) {
            sendEffect { GameEffect.InvalidWord }
            sendEffect { GameEffect.RowShake }
            return
        }

        val rawGuess = state.board[state.currentRow].map { it.letter }.joinToString("")

        viewModelScope.launch {
            setState { copy(isValidating = true) }
            val isValid = validateWordUseCase(rawGuess, state.language, state.wordList)
            setState { copy(isValidating = false) }

            if (!isValid) {
                sendEffect { GameEffect.NotInWordList }
                sendEffect { GameEffect.RowShake }
                return@launch
            }

            // Re-read state in case it changed while validating
            val s = uiState.value
            val evaluatedRow      = evaluateGuess(rawGuess, s.targetWord)
            val newBoard          = s.board.toMutableList().also { it[s.currentRow] = evaluatedRow }.toList()
            val newKeyboardStates = s.keyboardStates.mergeWith(evaluatedRow)

            val isWin  = evaluatedRow.all { it.state == TileState.CORRECT }
            val isLast = s.currentRow == s.board.size - 1

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
                if (isWin) recordWinUseCase(s.wordLength)
                sendEffect { GameEffect.ShowGameDialog(isWin = isWin, targetWord = s.targetWord) }
            }
        }
    }

    private fun restartGame() {
        val state = uiState.value
        setState {
            GameUiState(
                wordLength = state.wordLength,
                wordList   = state.wordList,
                targetWord = state.wordList.randomOrNull()?.normalizeForWordle() ?: "",
                board      = List(MAX_GUESSES) { List(state.wordLength) { Tile() } }
            )
        }
    }

    /**
     * Pairs of Arabic letters that are considered "similar":
     * ه (plain ha, U+0647) and ة (ta marbuta, U+0629) are phonetically
     * interchangeable to Arabic speakers and share the same base glyph.
     */
    private val similarPairs: List<Set<Char>> = listOf(
        setOf('\u0647', '\u0629') // ه ↔ ة
    )

    private fun areSimilarArabicLetters(a: Char, b: Char): Boolean =
        a != b && similarPairs.any { it.contains(a) && it.contains(b) }

    private fun evaluateGuess(guess: String, target: String): List<Tile> {
        val g               = guess.normalizeForWordle()
        val t               = target.normalizeForWordle()
        val wordLength      = t.length
        val result          = Array(wordLength) { TileState.WRONG }
        val targetArr       = t.toCharArray()
        val guessArr        = g.toCharArray()
        val remainingTarget = targetArr.toMutableList()

        // First pass: exact matches and similar-letter matches (right position)
        for (i in guessArr.indices) {
            when {
                guessArr[i] == targetArr[i] -> {
                    result[i]          = TileState.CORRECT
                    remainingTarget[i] = '\u0000'
                }
                areSimilarArabicLetters(guessArr[i], targetArr[i]) -> {
                    result[i]          = TileState.SIMILAR
                    remainingTarget[i] = '\u0000'
                }
            }
        }
        // Second pass: misplaced letters (skip CORRECT and SIMILAR positions)
        for (i in guessArr.indices) {
            if (result[i] == TileState.CORRECT || result[i] == TileState.SIMILAR) continue
            val idx = remainingTarget.indexOf(guessArr[i])
            if (idx != -1) {
                result[i]            = TileState.MISPLACED
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
            TileState.CORRECT   to 5,
            TileState.SIMILAR   to 4,
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

    private fun useHint() {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.hintsUsed >= state.maxHints) return
        if (state.targetWord.isEmpty()) return

        // Find an index not yet correct in the current row
        val currentRowTiles = state.board[state.currentRow]
        val hintIndex = state.targetWord.indices.firstOrNull { index ->
            currentRowTiles[index].state != TileState.CORRECT
        } ?: return

        val hintLetter = state.targetWord[hintIndex]

        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = hintIndex,
            tile = Tile(letter = hintLetter, state = TileState.CORRECT)
        )

        setState {
            copy(
                board      = newBoard,
                hintsUsed  = hintsUsed + 1,
                // Advance currentCol past the hint tile if needed
                currentCol = if (hintIndex >= currentCol) hintIndex + 1 else currentCol
            )
        }
    }

}