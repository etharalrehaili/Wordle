package com.khammin.game.domain.usecases.profile

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/** Fetches a user profile by Firebase UID. Returns cached data if available. */
class GetProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(firebaseUid: String): Resource<Profile?> {
        return try {
            Resource.Success(repo.getProfile(firebaseUid))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}