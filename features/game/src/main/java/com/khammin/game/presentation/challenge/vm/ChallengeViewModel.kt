package com.khammin.game.presentation.challenge.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.core.util.Resource
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.usecases.challenge.GetDailyChallengeUseCase
import com.khammin.game.domain.usecases.challenge.LoadTodayChallengeUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.challenge.SaveChallengeStateUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.presentation.challenge.contract.ChallengeEffect
import com.khammin.game.presentation.challenge.contract.ChallengeIntent
import com.khammin.game.presentation.challenge.contract.ChallengeUiState
import com.khammin.game.presentation.game.contract.Tile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.compareTo
import kotlin.text.set

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
                val wordLength = saved.targetWord.length
                when (val wordsResult = getWordsUseCase(language, wordLength)) {
                    is Resource.Success -> {
                        val targetNorm = saved.targetWord.normalizeForWordle()
                        val list =
                            wordsResult.data.withDailyTargetIfMissing(targetNorm)
                        setState {
                            copy(
                                isLoading      = false,
                                language       = language,
                                wordLength     = wordLength,
                                targetWord     = saved.targetWord,
                                wordList       = list,
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
                    }
                    is Resource.Error   -> setState { copy(isLoading = false, error = wordsResult.message) }
                    is Resource.Loading -> Unit
                }
            } else {
                // Fetch fresh challenge for the requested language, then the dictionary for guesses
                when (val challengeResult = getDailyChallengeUseCase(today, language)) {
                    is Resource.Success -> {
                        val target = challengeResult.data
                        val wordLength = target.length
                        when (val wordsResult = getWordsUseCase(language, wordLength)) {
                            is Resource.Success -> {
                                val targetNorm = target.normalizeForWordle()
                                val list =
                                    wordsResult.data.withDailyTargetIfMissing(targetNorm)
                                setState {
                                    copy(
                                    isLoading = false,
                                    language = language,
                                    targetWord = target,
                                    wordLength = wordLength,
                                    wordList = list,
                                    board = List(MAX_GUESSES) { List(wordLength) { Tile() } },
                                    keyboardStates = emptyMap(),
                                    currentRow = 0,
                                    currentCol = 0,
                                    isGameOver = false,
                                )
                                }
                            }
                            is Resource.Error   -> setState { copy(isLoading = false, error = wordsResult.message) }
                            is Resource.Loading -> Unit
                        }
                    }
                    is Resource.Error   -> setState { copy(isLoading = false, error = challengeResult.message) }
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

        val rawGuess     = state.board[state.currentRow].map { it.letter }.joinToString("")
        val guessNorm    = rawGuess.normalizeForWordle()
        val isInWordList = state.wordList.any { it.normalizeForWordle() == guessNorm }
        if (!isInWordList) {
            sendEffect { ChallengeEffect.NotInWordList }
            sendEffect { ChallengeEffect.RowShake }
            return
        }

        val evaluatedRow = evaluateGuess(rawGuess, state.targetWord)
        val newBoard = state.board.toMutableList().also { it[state.currentRow] = evaluatedRow }.toList()
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
            val newWinRate = if (newGamesPlayed > 0) (newWordsSolved * 100.0 / newGamesPlayed) else 0.0
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
        val g               = guess.normalizeForWordle()
        val t               = target.normalizeForWordle()
        val result          = Array(t.length) { TileState.WRONG }
        val targetArr       = t.toCharArray()
        val guessArr        = g.toCharArray()
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

    private fun List<String>.withDailyTargetIfMissing(targetNorm: String): List<String> {
        if (any { it.normalizeForWordle() == targetNorm }) return this
        return this + targetNorm
    }
}