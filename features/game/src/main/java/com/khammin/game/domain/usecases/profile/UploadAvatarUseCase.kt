package com.khammin.game.domain.usecases.profile

import android.content.Context
import android.net.Uri
import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/** Uploads a profile avatar image to Strapi and returns the hosted URL. */
class UploadAvatarUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke(imageUri: Uri, context: Context): Resource<String> {
        return try {
            Resource.Success(repo.uploadAvatar(imageUri, context))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}