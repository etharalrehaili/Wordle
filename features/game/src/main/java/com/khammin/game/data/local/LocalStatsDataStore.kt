package com.khammin.game.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "stats_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun recordGame(language: String, isWin: Boolean) {
        val playedKey = "${language}_stats_games_played"
        val solvedKey = "${language}_stats_words_solved"
        prefs.edit()
            .putInt(playedKey, prefs.getInt(playedKey, 0) + 1)
            .also { if (isWin) it.putInt(solvedKey, prefs.getInt(solvedKey, 0) + 1) }
            .apply()
    }

    fun getGamesPlayed(language: String): Int =
        prefs.getInt("${language}_stats_games_played", 0)

    fun getWordsSolved(language: String): Int =
        prefs.getInt("${language}_stats_words_solved", 0)

    fun getTotalPoints(): Int =
        prefs.getInt("stats_total_points", 0)

    fun addPoints(delta: Int) {
        prefs.edit().putInt("stats_total_points", getTotalPoints() + delta).apply()
    }

    fun observeTotalPoints(): Flow<Int> = callbackFlow {
        trySend(getTotalPoints())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "stats_total_points") trySend(getTotalPoints())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun clearAll() = prefs.edit().clear().apply()
}
