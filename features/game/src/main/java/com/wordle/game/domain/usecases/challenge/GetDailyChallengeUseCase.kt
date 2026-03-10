package com.wordle.game.domain.usecases.challenge

import com.wordle.core.util.Resource
import com.wordle.game.domain.repository.ChallengeRepository
import javax.inject.Inject

class GetDailyChallengeUseCase @Inject constructor(
    private val repo: ChallengeRepository
) {
    suspend operator fun invoke(date: String, language: String): Resource<String> {
        return try {
            val word = repo.getDailyChallenge(date, language)
            if (word == null) Resource.Error("No challenge for today")
            else Resource.Success(word)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}