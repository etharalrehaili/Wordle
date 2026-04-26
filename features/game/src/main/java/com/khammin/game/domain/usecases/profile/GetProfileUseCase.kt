package com.khammin.game.domain.usecases.profile

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/** Fetches a user profile by Firebase UID. Returns cached data if available.
 *  Set [forceRefresh] to true to bypass the local cache and fetch from the server. */
class GetProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(firebaseUid: String, forceRefresh: Boolean = false): Resource<Profile?> {
        return try {
            Resource.Success(repo.getProfile(firebaseUid, forceRefresh))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}