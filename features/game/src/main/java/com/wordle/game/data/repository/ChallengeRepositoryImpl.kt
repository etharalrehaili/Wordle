package com.wordle.game.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.presentation.components.MAX_GUESSES
import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.local.entity.ChallengeEntity
import com.wordle.game.data.remote.datasource.challenge.ChallengeRemoteDataSource
import com.wordle.game.domain.repository.ChallengeRepository
import com.wordle.game.presentation.game.contract.Tile
import com.wordle.game.presentation.game.contract.TileState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.challengeDataStore by preferencesDataStore(name = "challenge_prefs")

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remote: ChallengeRemoteDataSource,
    private val db: AppDatabase,
) : ChallengeRepository {

    companion object {
        private val KEY_UID          = stringPreferencesKey("challenge_uid")
        private val KEY_DATE         = stringPreferencesKey("challenge_date")
        private val KEY_TARGET       = stringPreferencesKey("challenge_target")
        private val KEY_CURRENT_ROW  = intPreferencesKey("challenge_current_row")
        private val KEY_CURRENT_COL  = intPreferencesKey("challenge_current_col")
        private val KEY_IS_GAME_OVER = booleanPreferencesKey("challenge_is_game_over")
        private val KEY_IS_WIN       = booleanPreferencesKey("challenge_is_win")
        private val KEY_BOARD        = stringPreferencesKey("challenge_board")
        private val KEY_KEYBOARD     = stringPreferencesKey("challenge_keyboard")
    }

    override suspend fun getDailyChallenge(date: String, language: String): String? {
        val cached = db.challengeDao().getChallenge(date, language)
        if (cached != null) return cached.word

        val word = remote.getDailyChallenge(date, language) ?: return null
        db.challengeDao().insertChallenge(
            ChallengeEntity(date = date, language = language, word = word)
        )
        db.challengeDao().deleteOldChallenges(date)
        return word
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loadTodayState(): SavedChallengeState? {
        val prefs      = context.challengeDataStore.data.first()
        val savedDate  = prefs[KEY_DATE] ?: return null
        val savedUid   = prefs[KEY_UID]  ?: return null
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        if (savedUid != currentUid) return null
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
            board          = decodeBoard(boardStr, target.length),
            keyboardStates = decodeKeyboard(keyboardStr),
            currentRow     = currentRow,
            currentCol     = currentCol,
            isGameOver     = isGameOver,
            isWin          = isWin,
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun saveState(
        targetWord: String,
        board: List<List<Tile>>,
        keyboardStates: Map<Char, TileState>,
        currentRow: Int,
        currentCol: Int,
        isGameOver: Boolean,
        isWin: Boolean,
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        context.challengeDataStore.edit { prefs ->
            prefs[KEY_UID]          = currentUid
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

    private fun encodeBoard(board: List<List<Tile>>): String =
        board.flatten().joinToString(",") { "${it.letter}:${it.state.name}" }

    private fun encodeKeyboard(map: Map<Char, TileState>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value.name}" }

    private fun decodeBoard(raw: String, wordLength: Int): List<List<Tile>> =
        raw.split(",").map { token ->
            val parts = token.split(":")
            Tile(letter = parts[0].first(), state = TileState.valueOf(parts[1]))
        }.chunked(wordLength).take(MAX_GUESSES)

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