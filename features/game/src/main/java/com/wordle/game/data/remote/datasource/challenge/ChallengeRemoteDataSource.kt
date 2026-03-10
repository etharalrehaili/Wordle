package com.wordle.game.data.remote.datasource.challenge

interface ChallengeRemoteDataSource {
    suspend fun getDailyChallenge(date: String, language: String): String?
}