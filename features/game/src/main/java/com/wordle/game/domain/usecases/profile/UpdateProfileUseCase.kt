package com.wordle.game.domain.usecases.profile

import com.wordle.core.util.Resource
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int = 0,
        wordsSolved: Int = 0,
        winPercentage: Double = 0.0,
        currentPoints: Int = 0,
    ): Resource<ProfileItem> {
        return try {
            val result = repository.updateProfile(
                documentId    = documentId,
                name          = name,
                avatarUrl     = avatarUrl,
                gamesPlayed   = gamesPlayed,
                wordsSolved   = wordsSolved,
                winPercentage = winPercentage,
                currentPoints = currentPoints,
            )
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}