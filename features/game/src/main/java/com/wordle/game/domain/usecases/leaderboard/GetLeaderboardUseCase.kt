package com.wordle.game.domain.usecases.leaderboard

import com.wordle.core.util.Resource
import com.wordle.game.domain.model.Profile
import com.wordle.game.domain.repository.ProfileRepository
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(limit: Int = 10): Resource<List<Profile>> {
        return try {
            Resource.Success(repository.getLeaderboard(limit))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}