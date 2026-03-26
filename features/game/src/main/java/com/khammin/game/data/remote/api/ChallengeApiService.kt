package com.khammin.game.data.remote.api

import com.khammin.game.data.remote.model.DailyChallengeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChallengeApiService {
    @GET("daily-challenges")
    suspend fun getDailyChallenge(
        @Query("filters[date][\$contains]") date: String,
        @Query("filters[language][\$eq]") language: String,
    ): DailyChallengeResponse
}