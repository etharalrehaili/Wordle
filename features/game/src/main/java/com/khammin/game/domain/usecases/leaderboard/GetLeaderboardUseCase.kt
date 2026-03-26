package com.khammin.game.domain.usecases.leaderboard

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(limit: Int = 10, language: String = "en"): Resource<List<Profile>> {
        return try {
            Resource.Success(repository.getLeaderboard(limit, language))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}