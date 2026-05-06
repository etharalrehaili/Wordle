package com.khammin.game.domain.usecases.profile

import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.model.ProfileUpdate
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(update: ProfileUpdate): Resource<Profile> {
        return try {
            Resource.Success(repository.updateProfile(update))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}