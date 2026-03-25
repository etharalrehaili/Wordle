package com.wordle.game.presentation.challenge.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.mvi.BaseMviViewModel
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.presentation.components.enums.TileState
import com.wordle.core.util.Resource
import com.wordle.game.domain.usecases.challenge.GetDailyChallengeUseCase
import com.wordle.game.domain.usecases.profile.GetProfileUseCase
import com.wordle.game.domain.usecases.challenge.LoadTodayChallengeUseCase
import com.wordle.game.domain.usecases.challenge.SaveChallengeStateUseCase
import com.wordle.game.domain.usecases.game.GetWordsUseCase
import com.wordle.game.domain.usecases.profile.UpdateProfileUseCase
import com.wordle.game.presentation.challenge.contract.ChallengeEffect
import com.wordle.game.presentation.challenge.contract.ChallengeIntent
import com.wordle.game.presentation.challenge.contract.ChallengeUiState
import com.wordle.game.presentation.game.contract.GameEffect
import com.wordle.game.presentation.game.contract.Tile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val getDailyChallengeUseCase: GetDailyChallengeUseCase,
    private val getWordsUseCase: GetWordsUseCase,
    private val loadTodayChallengeUseCase: LoadTodayChallengeUseCase,
    private val saveChallengeStateUseCase: SaveChallengeStateUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : BaseMviViewModel<ChallengeIntent, ChallengeUiState, ChallengeEffect>(
    initialState = ChallengeUiState()
) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEvent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.LoadWords    -> loadWords(intent.language)
            is ChallengeIntent.EnterLetter  -> enterLetter(intent.letter)
            is ChallengeIntent.DeleteLetter -> deleteLetter()
            is ChallengeIntent.SubmitGuess  -> submitGuess()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadWords(language: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val today = LocalDate.now().toString()

            val saved = loadTodayChallengeUseCase(language)

            // Only restore saved state if it matches the requested language
            if (saved != null && saved.language == language) {
                setState {
                    copy(
                        isLoading      = false,
                        language       = language,
                        wordLength     = saved.targetWord.length,
                        targetWord     = saved.targetWord,
                        board          = saved.board,
                        keyboardStates = saved.keyboardStates,
                        currentRow     = saved.currentRow,
                        currentCol     = saved.currentCol,
                        isGameOver     = saved.isGameOver,
                    )
                }
                if (saved.isGameOver) {
                    sendEffect { ChallengeEffect.ShowGameDialog(isWin = saved.isWin, targetWord = saved.targetWord) }
                }
            } else {
                // Fetch fresh challenge for the requested language
                when (val result = getDailyChallengeUseCase(today, language)) {
                    is Resource.Success -> setState {
                        copy(
                            isLoading  = false,
                            language = language,
                            targetWord = result.data,
                            wordLength = result.data.length,
                            board      = List(MAX_GUESSES) { List(result.data.length) { Tile() } },
                            keyboardStates = emptyMap(),
                            currentRow = 0,
                            currentCol = 0,
                            isGameOver = false,
                        )
                    }
                    is Resource.Error   -> setState { copy(isLoading = false, error = result.message) }
                    is Resource.Loading -> Unit
                }
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
            sendEffect { ChallengeEffect.InvalidWord }
            sendEffect { ChallengeEffect.RowShake }
            return
        }

        val guess        = state.board[state.currentRow].map { it.letter }.joinToString("")

        val isInWordList = state.wordList.any { it.equals(guess, ignoreCase = true) }
        if (!isInWordList) {
            sendEffect { ChallengeEffect.NotInWordList }
            sendEffect { ChallengeEffect.RowShake }
            return
        }

        val evaluatedRow = evaluateGuess(guess, state.targetWord)
        val newBoard     = state.board.toMutableList().also { it[state.currentRow] = evaluatedRow }.toList()
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
                language       = s.language,
                targetWord     = s.targetWord,
                board          = s.board,
                keyboardStates = s.keyboardStates,
                currentRow     = s.currentRow,
                currentCol     = s.currentCol,
                isGameOver     = s.isGameOver,
                isWin          = isWin,
            )
        }

        val currentLanguage = state.language
        if (isWin || isLast) {
            sendEffect { ChallengeEffect.ShowGameDialog(isWin = isWin, targetWord = state.targetWord) }
            updateProfileStats(isWin = isWin, guessCount = state.currentRow + 1, language = currentLanguage)
        }
    }

    private fun updateProfileStats(isWin: Boolean, guessCount: Int, language: String) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val profile = when (val result = getProfileUseCase(user.uid)) {
                is Resource.Success -> result.data ?: return@launch
                else -> return@launch
            }

            val pointsEarned = if (isWin) when (guessCount) {
                1    -> 100
                2    -> 80
                3    -> 60
                4    -> 40
                5    -> 20
                else -> 10
            } else 0

            val currentGames    = profile.gamesPlayedForLanguage(language)
            val currentSolved   = profile.wordsSolvedForLanguage(language)
            val currentPoints   = profile.pointsForLanguage(language)

            val newGamesPlayed  = currentGames + 1
            val newWordsSolved  = currentSolved + if (isWin) 1 else 0
            val newWinRate      = if (newGamesPlayed > 0) (newWordsSolved * 100.0 / newGamesPlayed) else 0.0
            val newPoints       = currentPoints + pointsEarned

            updateProfileUseCase(
                documentId    = profile.documentId,
                firebaseUid   = user.uid,
                name          = profile.name,
                avatarUrl     = profile.avatarUrl,
                language      = language,
                gamesPlayed   = newGamesPlayed,
                wordsSolved   = newWordsSolved,
                winPercentage = newWinRate,
                currentPoints = newPoints,
            )
        }
    }

    private fun evaluateGuess(guess: String, target: String): List<Tile> {
        val result          = Array(target.length) { TileState.WRONG }
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