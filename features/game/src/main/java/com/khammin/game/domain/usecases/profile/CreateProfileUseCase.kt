package com.khammin.game.domain.usecases.profile

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/** Creates a new Strapi profile on first login using Firebase UID and email username. */
class CreateProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(firebaseUid: String, email: String): Resource<Profile> {
        return try {
            Resource.Success(repo.createProfile(firebaseUid, email))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}