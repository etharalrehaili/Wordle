package com.wordle.game.data.remote.datasource.profile

import android.content.Context
import android.net.Uri
import com.wordle.game.data.remote.api.CreateProfileData
import com.wordle.game.data.remote.api.CreateProfileRequest
import com.wordle.game.data.remote.api.ProfileApiService
import com.wordle.game.data.remote.api.UpdateProfileData
import com.wordle.game.data.remote.api.UpdateProfileRequest
import com.wordle.game.data.remote.model.ProfileItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val api: ProfileApiService
) : ProfileRemoteDataSource {

    override suspend fun getProfile(firebaseUid: String): ProfileItem? {
        return api.getProfile(firebaseUid).data.firstOrNull()
    }

    override suspend fun createProfile(firebaseUid: String, name: String): ProfileItem {
        return api.createProfile(
            CreateProfileRequest(CreateProfileData(firebaseUid, name))
        ).data
    }

    override suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem {
        return api.updateProfile(
            documentId,
            UpdateProfileRequest(UpdateProfileData(name, avatarUrl, gamesPlayed, wordsSolved, winPercentage, currentPoints))
        ).data
    }

    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")
        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            else         -> "jpg"
        }
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("files", "avatar.$extension", requestBody)

        val response = api.uploadAvatar(part)
        val relativePath = response.first().url

        return "http://10.0.2.2:1337$relativePath"
    }

    override suspend fun getLeaderboard(limit: Int): List<ProfileItem> {
        return api.getLeaderboard(limit = limit).data
    }
}