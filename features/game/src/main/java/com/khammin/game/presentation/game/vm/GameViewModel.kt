package com.khammin.game.presentation.game.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.domain.model.TileState
import com.khammin.core.util.Resource
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.model.GameMode
import com.khammin.game.domain.model.GameResult
import com.khammin.game.domain.usecases.challenges.AwardChallengePointsUseCase
import com.khammin.game.domain.usecases.challenges.EvaluateChallengesUseCase
import com.khammin.game.domain.usecases.game.GetWordsUseCase
import com.khammin.game.domain.usecases.game.RecordWinUseCase
import com.khammin.game.domain.usecases.game.ValidateWordUseCase
import com.khammin.game.domain.usecases.stats.RecordGameUseCase
import com.khammin.game.presentation.game.contract.GameEffect
import com.khammin.game.presentation.game.contract.GameIntent
import com.khammin.game.presentation.game.contract.GameUiState
import com.khammin.game.domain.model.Tile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the solo (single-player) Wordle game.
 *
 * Injected use cases and their roles:
 * - [GetWordsUseCase]             — loads the word list for a given language + length (cache-first).
 * - [ValidateWordUseCase]         — validates a guess: local list first, then remote API (5 s cap).
 * - [RecordWinUseCase]            — persists a win streak / best-score update locally (win only).
 * - [RecordGameUseCase]           — records the full game result to the remote stats service.
 * - [EvaluateChallengesUseCase]   — checks whether the result satisfies any active challenges.
 * - [AwardChallengePointsUseCase] — credits points for every newly completed challenge.
 *
 * State is exposed as [GameUiState]; one-shot events via [GameEffect].
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val recordWinUseCase: RecordWinUseCase,
    private val validateWordUseCase: ValidateWordUseCase,
    private val evaluateChallengesUseCase: EvaluateChallengesUseCase,
    private val awardChallengePointsUseCase: AwardChallengePointsUseCase,
    private val recordGameUseCase: RecordGameUseCase,
) : BaseMviViewModel<GameIntent, GameUiState, GameEffect>(
    initialState = GameUiState()
) {

    /** Wall-clock millis captured when a word is first shown; used to compute elapsed game time. */
    private var gameStartTime = 0L

    override fun onEvent(intent: GameIntent) {
        when (intent) {
            is GameIntent.LoadWords    -> loadWords(intent.language, intent.wordLength)
            is GameIntent.EnterLetter  -> enterLetter(intent.letter)
            is GameIntent.DeleteLetter -> deleteLetter()
            // SubmitGuess is not dispatched by the UI directly — enterLetter() auto-submits
            // when the last letter is placed. The intent exists for external callers (e.g. tests).
            is GameIntent.SubmitGuess  -> submitGuess()
            is GameIntent.RestartGame  -> restartGame()
            // UseHint / EarnHint are fully implemented but the hint button is temporarily
            // commented out in GameScreen. Re-enable onHintClicked in GameTopBar to activate.
            is GameIntent.UseHint      -> useHint()
            is GameIntent.EarnHint     -> earnHint()
        }
    }

    /**
     * Fetches the word list for [language] + [wordLength] and picks a random target word.
     *
     * Guard: skips the fetch if a valid word list is already in state for the same length,
     * preventing redundant network calls when the composable recomposes.
     *
     * Each word now carries an optional [WordData.meaning] field pulled from Strapi.
     * The meaning is stored alongside the target word so it can be shown in the
     * result bottom sheet without a second network call.
     */
    private fun loadWords(language: String, wordLength: Int) {
        val current = uiState.value
        Log.d("GameDebug", "loadWords() called — language=$language, wordLength=$wordLength")
        Log.d("GameDebug", "loadWords() state — isLoading=${current.isLoading}, isGameOver=${current.isGameOver}, targetWord='${current.targetWord}', wordLength=${current.wordLength}")

        if (current.wordList.isNotEmpty() && current.wordLength == wordLength && current.targetWord.isNotEmpty()) {
            Log.d("GameDebug", "loadWords() skipped — words already cached (${current.wordList.size} words, targetWord='${current.targetWord}')")
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            Log.d("GameDebug", "loadWords() fetching words from repository…")
            when (val result = getWordsUseCase(language, wordLength)) {
                is Resource.Success -> {
                    val words    = result.data
                    val selected = words.random()
                    Log.d("GameDebug", "loadWords() success — ${words.size} words loaded, selectedWord='${selected.text}', meaning='${selected.meaning}'")
                    setState {
                        copy(
                            isLoading         = false,
                            language          = language,
                            wordLength        = wordLength,
                            wordList          = words,
                            targetWord        = selected.text.normalizeForWordle(),
                            // Store the meaning so the result sheet can display it
                            // without fetching again. Null when Strapi has no meaning yet.
                            targetWordMeaning = selected.meaning,
                            board             = List(MAX_GUESSES) { List(wordLength) { Tile() } }
                        )
                    }
                    Log.d("GameDebug", "loadWords() state updated — targetWord='${selected.text.normalizeForWordle()}'")
                    gameStartTime = System.currentTimeMillis()
                }
                is Resource.Error -> {
                    Log.d("GameDebug", "loadWords() error — ${result.message}")
                    setState { copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Places [letter] in the current cell and auto-submits when the row is full.
     *
     * Blocked while the game is over, a validation is in progress, or no target word
     * has been loaded yet (prevents input before the word list arrives from the network).
     */
    private fun enterLetter(letter: Char) {
        val state = uiState.value
        Log.d("GameDebug", "enterLetter('$letter') — isGameOver=${state.isGameOver}, isValidating=${state.isValidating}, targetWord='${state.targetWord}', currentCol=${state.currentCol}, wordLength=${state.wordLength}")
        if (state.isGameOver || state.isValidating) {
            Log.d("GameDebug", "enterLetter() BLOCKED — isGameOver=${state.isGameOver}, isValidating=${state.isValidating}")
            return
        }
        if (state.targetWord.isEmpty()) {
            Log.d("GameDebug", "enterLetter() BLOCKED — targetWord is empty (words not loaded yet)")
            return
        }
        if (state.currentCol >= state.wordLength) {
            Log.d("GameDebug", "enterLetter() BLOCKED — currentCol(${state.currentCol}) >= wordLength(${state.wordLength})")
            return
        }

        val newBoard = state.board.updateTile(
            row  = state.currentRow,
            col  = state.currentCol,
            tile = Tile(letter = letter.uppercaseChar(), state = TileState.FILLED)
        )
        val newCol = state.currentCol + 1
        setState { copy(board = newBoard, currentCol = newCol) }

        // Auto-submit when the last cell is filled so the user never needs a separate submit key.
        if (newCol == state.wordLength) submitGuess()
    }

    /** Removes the letter in the previous cell. Blocked while validating or at column 0. */
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

    /**
     * Validates the completed row and, if valid, evaluates the guess against the target.
     *
     * Validation order:
     * 1. Skip if the row is not yet full (can happen if [GameIntent.SubmitGuess] is sent externally).
     * 2. Normalize both strings once and short-circuit if the guess exactly matches the target
     *    (avoids an unnecessary network call when the player types the correct answer).
     * 3. Otherwise delegate to [ValidateWordUseCase], which checks the local word list first
     *    then falls back to the remote API (capped at 5 s; returns false on timeout or offline).
     *
     * On game-over, [GameEffect.ShowGameDialog] carries both the target word and its meaning
     * so the result sheet can display them immediately without an extra fetch.
     *
     * Stats and challenge evaluation are each launched as independent fire-and-forget
     * coroutines so they don't delay [GameEffect.ShowGameDialog].
     */
    private fun submitGuess() {
        val state = uiState.value
        if (state.isGameOver || state.isValidating) return
        if (state.currentCol < state.wordLength) return

        val rawGuess = state.board[state.currentRow].map { it.letter }.joinToString("")

        viewModelScope.launch {
            setState { copy(isValidating = true) }

            // Normalize once here; reused for the target-match shortcut and passed into
            // evaluateGuess, so the strings are never normalized a second time.
            val normalizedGuess  = rawGuess.normalizeForWordle()
            val normalizedTarget = state.targetWord.normalizeForWordle()

            val isTargetMatch = normalizedGuess == normalizedTarget
            // rawGuess (not normalized) is passed to the API because some endpoints are
            // diacritic-sensitive; ValidateWordUseCase handles local normalization internally.
            val isValid = isTargetMatch || validateWordUseCase(
                rawGuess, state.language,
                // Pass only the text strings for validation — meaning is not needed here.
                state.wordList.map { it.text }
            )
            setState { copy(isValidating = false) }

            if (!isValid) {
                sendEffect { GameEffect.NotInWordList }
                return@launch
            }

            // Re-read state in case it changed while the async validation was running.
            val s = uiState.value
            val evaluatedRow      = evaluateGuess(normalizedGuess, normalizedTarget)
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

                // Fire-and-forget: independent coroutines so neither blocks the dialog effect.
                viewModelScope.launch { recordGameUseCase(s.language, isWin) }

                // Carry the meaning into the effect so GameResultsBottomSheet can show
                // it immediately — no second network call required.
                sendEffect {
                    GameEffect.ShowGameDialog(
                        isWin      = isWin,
                        targetWord = s.targetWord,
                        meaning    = s.targetWordMeaning
                    )
                }

                val elapsed = if (gameStartTime > 0L)
                    (System.currentTimeMillis() - gameStartTime) / 1000L else Long.MAX_VALUE

                viewModelScope.launch {
                    val gameResult = GameResult(
                        isWin            = isWin,
                        guessCount       = s.currentRow + 1,
                        timeTakenSeconds = elapsed,
                        wordLength       = s.wordLength,
                        gameMode         = GameMode.SOLO,
                        hintsUsed        = s.hintsUsed,
                        language         = s.language,
                    )
                    // runCatching: challenge evaluation failing must never crash the game.
                    runCatching {
                        val completed = evaluateChallengesUseCase(gameResult)
                        awardChallengePointsUseCase(completed)
                    }
                }
            }
        }
    }

    /**
     * Resets the board for a fresh game, reusing the already-loaded word list.
     * Picking from the existing list avoids a redundant network round-trip on restart.
     * The new target word's meaning is stored so the next result sheet can show it.
     */
    private fun restartGame() {
        val state    = uiState.value
        val selected = state.wordList.randomOrNull()
        setState {
            GameUiState(
                wordLength        = state.wordLength,
                wordList          = state.wordList,
                language          = state.language,
                targetWord        = selected?.text?.normalizeForWordle() ?: "",
                // Carry the new word's meaning forward for the next result sheet.
                targetWordMeaning = selected?.meaning,
                board             = List(MAX_GUESSES) { List(state.wordLength) { Tile() } }
            )
        }
        gameStartTime = System.currentTimeMillis()
    }

    /**
     * Arabic letter pairs treated as "similar" (right position, partial credit).
     * ه (U+0647, plain ha) and ة (U+0629, ta marbuta) are phonetically interchangeable
     * and share the same base glyph, so guessing one when the answer is the other earns
     * a SIMILAR tile instead of WRONG.
     */
    private val similarPairs: List<Set<Char>> = listOf(
        setOf('\u0647', '\u0629') // ه ↔ ة
    )

    private fun areSimilarArabicLetters(a: Char, b: Char): Boolean =
        a != b && similarPairs.any { it.contains(a) && it.contains(b) }

    /**
     * Colours each letter in [guess] against [target] using a two-pass algorithm.
     *
     * Both strings must already be normalized (no diacritics) before calling this function;
     * [submitGuess] normalizes once and passes the results here directly.
     *
     * Pass 1 — right position: marks CORRECT (exact match) or SIMILAR (phonetically close).
     *   Consumed target positions are zeroed out so they aren't double-counted in pass 2.
     *
     * Pass 2 — wrong position: for each remaining letter, marks MISPLACED if the letter
     *   exists elsewhere in the target. Each target letter can only satisfy one guess letter.
     */
    private fun evaluateGuess(guess: String, target: String): List<Tile> {
        val wordLength      = target.length
        val result          = Array(wordLength) { TileState.WRONG }
        val targetArr       = target.toCharArray()
        val guessArr        = guess.toCharArray()
        val remainingTarget = targetArr.toMutableList()

        // Pass 1: exact and similar matches at the correct position
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
        // Pass 2: misplaced letters (skip already-resolved positions)
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

    /**
     * Returns a new board with the tile at ([row], [col]) replaced by [tile].
     * The board is an immutable list-of-lists; this creates the minimum new objects needed.
     */
    private fun List<List<Tile>>.updateTile(row: Int, col: Int, tile: Tile): List<List<Tile>> =
        mapIndexed { r, rowList ->
            if (r != row) rowList
            else rowList.mapIndexed { c, t -> if (c == col) tile else t }
        }

    /**
     * Merges a newly evaluated [row] into the keyboard colour map using priority order:
     * CORRECT (5) > SIMILAR (4) > MISPLACED (3) > WRONG (2) > FILLED (1) > EMPTY (0).
     *
     * A key's colour can only improve, never regress — a correctly-guessed letter stays
     * green even if it appears as wrong in a later row.
     */
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

    /**
     * Reveals the leftmost un-revealed letter in the current row as a CORRECT tile,
     * consuming one hint from [GameUiState.maxHints].
     *
     * If revealing the hint fills the last cell, the row is auto-submitted.
     *
     * NOTE: The hint button in GameScreen is currently commented out. Re-enable the
     * onHintClicked lambda in GameTopBar to expose this feature to the user.
     */
    private fun useHint() {
        val state = uiState.value
        if (state.isGameOver) return
        if (state.hintsUsed >= state.maxHints) return
        if (state.targetWord.isEmpty()) return

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
        // Advance currentCol past the revealed cell only if the hint landed at or beyond it.
        val newCol = if (hintIndex >= state.currentCol) hintIndex + 1 else state.currentCol

        setState {
            copy(
                board      = newBoard,
                hintsUsed  = hintsUsed + 1,
                currentCol = newCol
            )
        }
        if (newCol == state.wordLength) submitGuess()
    }

    /**
     * Grants one extra hint slot (earned via a rewarded ad) then immediately uses it.
     * Called from GameScreen after [AdManager.showHintAd] delivers the reward callback.
     */
    private fun earnHint() {
        setState { copy(maxHints = maxHints + 1) }
        useHint()
    }
}