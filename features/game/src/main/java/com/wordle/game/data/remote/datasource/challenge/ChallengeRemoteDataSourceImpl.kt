package com.wordle.game.data.remote.datasource.challenge

import android.util.Log
import com.wordle.core.util.normalizeForWordle
import com.wordle.game.data.remote.api.ChallengeApiService
import javax.inject.Inject

class ChallengeRemoteDataSourceImpl @Inject constructor(
    private val api: ChallengeApiService
) : ChallengeRemoteDataSource {

    override suspend fun getDailyChallenge(date: String, language: String): String? {
        return try {
            Log.d("ChallengeDS", "📡 Requesting: date=$date, language=$language")
            val response = api.getDailyChallenge(date, language)
            Log.d("ChallengeDS", "📡 Raw data: ${response.data}")
            response.data.firstOrNull()?.word?.normalizeForWordle()
        } catch (e: Exception) {
            Log.e("ChallengeDS", "💥 ${e.message}", e)
            null
        }
    }
}