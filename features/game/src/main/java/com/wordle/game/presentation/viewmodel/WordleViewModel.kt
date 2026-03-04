package com.wordle.game.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordle.core.presentation.components.GuessRow
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.presentation.components.WORD_LENGTH
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class WordleViewModel(application: Application) : AndroidViewModel(application) {

    // ── UI State ──────────────────────────────────────────────────────────────

    private val _guesses = MutableStateFlow(List(MAX_GUESSES) { GuessRow() })
    val guesses: StateFlow<List<GuessRow>> = _guesses.asStateFlow()

    private val _currentRow = MutableStateFlow(0)
    val currentRow: StateFlow<Int> = _currentRow.asStateFlow()

    private val _currentCol = MutableStateFlow(0)
    val currentCol: StateFlow<Int> = _currentCol.asStateFlow()

    private val _keyStates = MutableStateFlow<Map<Char, Types>>(emptyMap())
    val keyStates: StateFlow<Map<Char, Types>> = _keyStates.asStateFlow()

    private val _gameState = MutableStateFlow<GameState>(GameState.Playing)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isReady = MutableStateFlow(true)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    // ── Internal game data ────────────────────────────────────────────────────

    private var wordList: List<String> = emptyList()
    private var targetWord: String = ""

    fun loadWords(language: AppLanguage) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val fileName = when (language) {
                    AppLanguage.ENGLISH -> "words.json"
                    AppLanguage.ARABIC  -> "arabWords.json"
                }
                val json = context.assets.open(fileName).bufferedReader().readText()
                val array = JSONObject(json).getJSONArray("words")
                wordList = List(array.length()) { i ->
                    val word = array.getString(i)
                    if (language == AppLanguage.ARABIC) word else word.uppercase()
                }.filter { it.length == WORD_LENGTH }

                android.util.Log.d("Wordle", "wordList size=${wordList.size}")

                if (wordList.isEmpty()) {
                    android.util.Log.e("Wordle", "wordList is EMPTY — check arabWords.json and WORD_LENGTH=$WORD_LENGTH")
                    return@launch
                }

                targetWord = wordList.random()
                android.util.Log.d("Wordle", "targetWord='$targetWord'")
                _isReady.update { true }
                // Reset game state when language changes
                _guesses.update { List(MAX_GUESSES) { GuessRow() } }
                _currentRow.update { 0 }
                _currentCol.update { 0 }
                _keyStates.update { emptyMap() }
                _gameState.update { GameState.Playing }
            } catch (e: Exception) {
                android.util.Log.e("Wordle", "loadWords FAILED", e)
            }
        }
    }

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onKey(char: Char) {
        if (_gameState.value != GameState.Playing) return
        val col = _currentCol.value
        val row = _currentRow.value
        if (col >= WORD_LENGTH) return

        val finalChar = if (char in 'A'..'Z' || char in 'a'..'z') char.uppercaseChar() else char
        val updatedLetters = _guesses.value[row].letters.toMutableList().also { it[col] = finalChar }

        _guesses.update { rows ->
            rows.toMutableList().also { list ->
                list[row] = list[row].copy(letters = updatedLetters)
            }
        }

        val newCol = col + 1
        _currentCol.update { newCol }

        // Pass the word we already computed — don't re-read from the flow
        if (newCol == WORD_LENGTH) {
            val guessWord = updatedLetters.joinToString("") { it?.toString() ?: "" }
            submitGuess(guessWord)          // <-- pass it in
        }
    }

    fun onBackspace() {
        if (_gameState.value != GameState.Playing) return
        val col = _currentCol.value
        val row = _currentRow.value
        if (col <= 0) return                                // nothing to delete

        val newCol = col - 1
        _guesses.update { rows ->
            rows.toMutableList().also { list ->
                val current    = list[row]
                val newLetters = current.letters.toMutableList().also { it[newCol] = null }
                list[row] = current.copy(letters = newLetters)
            }
        }
        _currentCol.update { newCol }
    }

    // Called by onKey (auto-submit) with a pre-built word
    private fun submitGuess(guessWord: String) {
        android.util.Log.d("Wordle", "submitGuess: guessWord='$guessWord', targetWord='$targetWord'")
        if (guessWord.length != WORD_LENGTH) return
        val row          = _currentRow.value
        val currentGuess = _guesses.value[row]

        val types = evaluateGuess(guessWord, targetWord)

        _guesses.update { rows ->
            rows.toMutableList().also { it[row] = currentGuess.copy(types = types) }
        }

        _keyStates.update { current ->
            current.toMutableMap().also { map ->
                guessWord.forEachIndexed { i, char ->
                    val existing = map[char] ?: Types.DEFAULT
                    val newType  = types[i]
                    if (newType.rank > existing.rank) map[char] = newType
                }
            }
        }

        when {
            guessWord == targetWord -> _gameState.update { GameState.Won(row + 1, targetWord) }
            row + 1 >= MAX_GUESSES  -> {
                android.util.Log.d("Wordle", "submitGuess: emitting Lost, targetWord='$targetWord'")
                _gameState.update { GameState.Lost(targetWord) }
            }
            else -> {
                _currentRow.update { it + 1 }
                _currentCol.update { 0 }
            }
        }
    }

    // Called by the ENTER key — reads from the flow (safe here, no race)
    fun onEnter() {
        if (_gameState.value != GameState.Playing) return
        if (_currentCol.value < WORD_LENGTH) return
        val row       = _currentRow.value
        val guessWord = _guesses.value[row].letters.joinToString("") { it?.toString() ?: "" }
        submitGuess(guessWord)
    }

    // ── Restart — picks a NEW random word each time ───────────────────────────

    fun restartGame() {
        _guesses.update { List(MAX_GUESSES) { GuessRow() } }
        _currentRow.update { 0 }
        _currentCol.update { 0 }
        _keyStates.update { emptyMap() }
        _gameState.update { GameState.Playing }
        // Pick a different random word for the next game
        targetWord = wordList.randomOrNull() ?: targetWord
    }

    // ── Guess evaluation ──────────────────────────────────────────────────────

    private fun evaluateGuess(guess: String, target: String): List<Types> {
        if (guess.length != WORD_LENGTH || target.length != WORD_LENGTH)
            return List(WORD_LENGTH) { Types.ABSENT }
        val result      = MutableList(WORD_LENGTH) { Types.ABSENT }
        val targetChars = target.toMutableList()

        // Pass 1: exact matches
        for (i in 0 until WORD_LENGTH) {
            if (guess[i] == target[i]) {
                result[i]      = Types.CORRECT
                targetChars[i] = '*'               // consumed — won't match again
            }
        }

        // Pass 2: wrong position
        for (i in 0 until WORD_LENGTH) {
            if (result[i] == Types.CORRECT) continue
            val idx = targetChars.indexOf(guess[i])
            if (idx != -1) {
                result[i]         = Types.PRESENT
                targetChars[idx]  = '*'            // consumed
            }
        }

        return result
    }
}

// ── Game state ────────────────────────────────────────────────────────────────

sealed class GameState {
    object Playing : GameState()
    data class Won(val attemptsUsed: Int, val answer: String) : GameState()
    data class Lost(val answer: String) : GameState()
}

// ── Helper: rank Types so keyboard colours only ever upgrade ──────────────────

private val Types.rank: Int
    get() = when (this) {
        Types.DEFAULT -> 0
        Types.ABSENT  -> 1
        Types.PRESENT -> 2
        Types.CORRECT -> 3
    }