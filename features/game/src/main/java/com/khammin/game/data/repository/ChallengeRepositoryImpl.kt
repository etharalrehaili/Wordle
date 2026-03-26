package com.khammin.game.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.presentation.components.MAX_GUESSES
import com.khammin.core.util.normalizeForWordle
import com.khammin.core.presentation.components.enums.TileState
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.local.entity.ChallengeEntity
import com.khammin.game.data.remote.datasource.challenge.ChallengeRemoteDataSource
import com.khammin.game.domain.repository.ChallengeRepository
import com.khammin.game.presentation.game.contract.Tile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        private fun keyUid(lang: String)        = stringPreferencesKey("challenge_uid_$lang")
        private fun keyDate(lang: String)       = stringPreferencesKey("challenge_date_$lang")
        private fun keyTarget(lang: String)     = stringPreferencesKey("challenge_target_$lang")
        private fun keyCurrentRow(lang: String) = intPreferencesKey("challenge_current_row_$lang")
        private fun keyCurrentCol(lang: String) = intPreferencesKey("challenge_current_col_$lang")
        private fun keyIsGameOver(lang: String) = booleanPreferencesKey("challenge_is_game_over_$lang")
        private fun keyIsWin(lang: String)      = booleanPreferencesKey("challenge_is_win_$lang")
        private fun keyBoard(lang: String)      = stringPreferencesKey("challenge_board_$lang")
        private fun keyKeyboard(lang: String)   = stringPreferencesKey("challenge_keyboard_$lang")
    }

    override suspend fun getDailyChallenge(date: String, language: String): String? {
        val cached = db.challengeDao().getChallenge(date, language)
        if (cached != null) return cached.word.normalizeForWordle()

        val word = remote.getDailyChallenge(date, language) ?: return null
        db.challengeDao().insertChallenge(
            ChallengeEntity(date = date, language = language, word = word)
        )
        db.challengeDao().deleteOldChallenges(date)
        return word
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun loadTodayState(language: String): SavedChallengeState? {
        val prefs      = context.challengeDataStore.data.first()
        val savedDate  = prefs[keyDate(language)] ?: return null
        val savedUid   = prefs[keyUid(language)]  ?: return null
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        if (savedUid != currentUid) return null
        if (savedDate != LocalDate.now().toString()) return null

        val target     = prefs[keyTarget(language)]     ?: return null
        val currentRow = prefs[keyCurrentRow(language)] ?: 0
        val currentCol = prefs[keyCurrentCol(language)] ?: 0
        val isGameOver = prefs[keyIsGameOver(language)] ?: false
        val isWin      = prefs[keyIsWin(language)]      ?: false
        val boardStr   = prefs[keyBoard(language)]      ?: return null
        val keyboardStr= prefs[keyKeyboard(language)]   ?: ""

        return SavedChallengeState(
            language       = language,
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
        language: String,
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
            prefs[keyUid(language)]        = currentUid
            prefs[keyDate(language)]       = LocalDate.now().toString()
            prefs[keyTarget(language)]     = targetWord
            prefs[keyCurrentRow(language)] = currentRow
            prefs[keyCurrentCol(language)] = currentCol
            prefs[keyIsGameOver(language)] = isGameOver
            prefs[keyIsWin(language)]      = isWin
            prefs[keyBoard(language)]      = encodeBoard(board)
            prefs[keyKeyboard(language)]   = encodeKeyboard(keyboardStates)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun hasSolvedTodayChallenge(language: String): Flow<Boolean> =
        context.challengeDataStore.data.map { prefs ->
            val savedDate  = prefs[keyDate(language)]      ?: return@map false
            val savedUid   = prefs[keyUid(language)]       ?: return@map false
            val isGameOver = prefs[keyIsGameOver(language)] ?: return@map false
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@map false

            savedUid == currentUid
                    && savedDate == LocalDate.now().toString()
                    && isGameOver
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
    val language: String,
    val targetWord: String,
    val board: List<List<Tile>>,
    val keyboardStates: Map<Char, TileState>,
    val currentRow: Int,
    val currentCol: Int,
    val isGameOver: Boolean,
    val isWin: Boolean,
)