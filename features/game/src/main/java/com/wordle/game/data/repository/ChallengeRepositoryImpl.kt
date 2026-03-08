package com.wordle.game.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.core.presentation.components.WORD_LENGTH
import com.wordle.game.domain.repository.ChallengeRepository
import com.wordle.game.presentation.contract.Tile
import com.wordle.game.presentation.contract.TileState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.challengeDataStore by preferencesDataStore(name = "challenge_prefs")

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ChallengeRepository {

    companion object {
        private val KEY_DATE         = stringPreferencesKey("challenge_date")
        private val KEY_TARGET       = stringPreferencesKey("challenge_target")
        private val KEY_CURRENT_ROW  = intPreferencesKey("challenge_current_row")
        private val KEY_CURRENT_COL  = intPreferencesKey("challenge_current_col")
        private val KEY_IS_GAME_OVER = booleanPreferencesKey("challenge_is_game_over")
        private val KEY_IS_WIN       = booleanPreferencesKey("challenge_is_win")
        private val KEY_BOARD        = stringPreferencesKey("challenge_board")
        private val KEY_KEYBOARD     = stringPreferencesKey("challenge_keyboard")
    }

    override suspend fun loadTodayState(): SavedChallengeState? {
        val prefs     = context.challengeDataStore.data.first()
        val savedDate = prefs[KEY_DATE] ?: return null
        if (savedDate != LocalDate.now().toString()) return null

        val target     = prefs[KEY_TARGET]       ?: return null
        val currentRow = prefs[KEY_CURRENT_ROW]  ?: 0
        val currentCol = prefs[KEY_CURRENT_COL]  ?: 0
        val isGameOver = prefs[KEY_IS_GAME_OVER] ?: false
        val isWin      = prefs[KEY_IS_WIN]       ?: false
        val boardStr   = prefs[KEY_BOARD]        ?: return null
        val keyboardStr= prefs[KEY_KEYBOARD]     ?: ""

        return SavedChallengeState(
            targetWord     = target,
            board          = decodeBoard(boardStr),
            keyboardStates = decodeKeyboard(keyboardStr),
            currentRow     = currentRow,
            currentCol     = currentCol,
            isGameOver     = isGameOver,
            isWin          = isWin,
        )
    }

    override suspend fun saveState(
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    ) {
        context.challengeDataStore.edit { prefs ->
            prefs[KEY_DATE]         = LocalDate.now().toString()
            prefs[KEY_TARGET]       = targetWord
            prefs[KEY_CURRENT_ROW]  = currentRow
            prefs[KEY_CURRENT_COL]  = currentCol
            prefs[KEY_IS_GAME_OVER] = isGameOver
            prefs[KEY_IS_WIN]       = isWin
            prefs[KEY_BOARD]        = encodeBoard(board)
            prefs[KEY_KEYBOARD]     = encodeKeyboard(keyboardStates)
        }
    }

    // ─── Encode / Decode ─────────────────────────────────────────────────────

    private fun encodeBoard(board: List<List<Tile>>): String =
        board.flatten().joinToString(",") { "${it.letter}:${it.state.name}" }

    private fun decodeBoard(raw: String): List<List<Tile>> =
        raw.split(",").map { token ->
            val parts = token.split(":")
            Tile(letter = parts[0].first(), state = TileState.valueOf(parts[1]))
        }.chunked(WORD_LENGTH).take(MAX_GUESSES)

    private fun encodeKeyboard(map: Map<Char, TileState>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value.name}" }

    private fun decodeKeyboard(raw: String): Map<Char, TileState> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(",").associate { token ->
            val parts = token.split(":")
            parts[0].first() to TileState.valueOf(parts[1])
        }
    }
}

data class SavedChallengeState(
    val targetWord: String,
    val board: List<List<Tile>>,
    val keyboardStates: Map<Char, TileState>,
    val currentRow: Int,
    val currentCol: Int,
    val isGameOver: Boolean,
    val isWin: Boolean,
)