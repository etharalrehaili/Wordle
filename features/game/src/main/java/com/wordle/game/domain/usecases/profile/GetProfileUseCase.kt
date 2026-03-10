package com.wordle.game.domain.usecases.profile

import com.wordle.core.util.Resource
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(firebaseUid: String): Resource<ProfileItem?> {
        return try {
            Resource.Success(repo.getProfile(firebaseUid))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}