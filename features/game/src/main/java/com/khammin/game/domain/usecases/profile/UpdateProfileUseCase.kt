package com.khammin.game.domain.usecases.profile

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/** Updates profile display info and game statistics (stats, points, win rate). */
class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        documentId: String,
        firebaseUid: String,
        name: String,
        avatarUrl: String?,
        language: String,
        gamesPlayed: Int = 0,
        wordsSolved: Int = 0,
        winPercentage: Double = 0.0,
        currentPoints: Int = 0,
    ): Resource<Profile> {
        return try {
            Resource.Success(
                repository.updateProfile(
                    documentId    = documentId,
                    firebaseUid   = firebaseUid,
                    name          = name,
                    avatarUrl     = avatarUrl,
                    language      = language,
                    gamesPlayed   = gamesPlayed,
                    wordsSolved   = wordsSolved,
                    winPercentage = winPercentage,
                    currentPoints = currentPoints,
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}