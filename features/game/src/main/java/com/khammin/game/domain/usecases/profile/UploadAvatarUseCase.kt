package com.khammin.game.domain.usecases.profile

import android.net.Uri
import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(imageUri: Uri): Resource<String> {
        return try {
            Resource.Success(repo.uploadAvatar(imageUri))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}