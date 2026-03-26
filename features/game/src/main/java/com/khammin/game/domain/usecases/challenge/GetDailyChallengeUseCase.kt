package com.khammin.game.domain.usecases.challenge

import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.ChallengeRepository
import javax.inject.Inject

// fetches today's challenge word from the remote API (or local cache).
// Called once when the challenge screen opens to get the word the user needs to guess.

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