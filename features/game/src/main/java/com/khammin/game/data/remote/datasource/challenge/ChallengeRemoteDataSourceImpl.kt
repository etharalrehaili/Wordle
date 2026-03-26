package com.khammin.game.data.remote.datasource.challenge

import com.khammin.core.util.normalizeForWordle
import com.khammin.game.data.remote.api.ChallengeApiService
import javax.inject.Inject

class ChallengeRemoteDataSourceImpl @Inject constructor(
    private val api: ChallengeApiService
) : ChallengeRemoteDataSource {

    override suspend fun getDailyChallenge(date: String, language: String): String? {
        return try {
            val response = api.getDailyChallenge(date, language)
            response.data.firstOrNull()?.word?.normalizeForWordle()
        } catch (e: Exception) {
            null
        }
    }
}