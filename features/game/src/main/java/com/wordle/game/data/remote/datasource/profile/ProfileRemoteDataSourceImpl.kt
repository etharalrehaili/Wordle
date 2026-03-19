package com.wordle.game.data.remote.datasource.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import com.wordle.game.data.remote.api.ProfileApiService
import com.wordle.game.data.remote.model.CreateProfileData
import com.wordle.game.data.remote.model.CreateProfileRequest
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.data.remote.model.UpdateProfileData
import com.wordle.game.data.remote.model.UpdateProfileRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * Concrete implementation of [ProfileRemoteDataSource].
 * Communicates with the Strapi backend via [ProfileApiService].
 */
class ProfileRemoteDataSourceImpl @Inject constructor(
    private val api: ProfileApiService
) : ProfileRemoteDataSource {

    /** Queries Strapi for a profile with matching firebaseUid. Returns first result or null. */
    override suspend fun getProfile(firebaseUid: String): ProfileItem? {
        return api.getProfile(firebaseUid).data.firstOrNull()
    }

    /** Creates a new profile in Strapi with the user's Firebase UID and display name. */
    override suspend fun createProfile(firebaseUid: String, name: String): ProfileItem {
        return api.createProfile(
            CreateProfileRequest(CreateProfileData(firebaseUid, name))
        ).data
    }

    /** Sends a PUT request to Strapi to update the profile document. */
    override suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem {
        try {
            return api.updateProfile(
                documentId,
                UpdateProfileRequest(
                    UpdateProfileData(
                        name          = name,
                        avatarUrl     = avatarUrl,
                        gamesPlayed   = gamesPlayed,
                        wordsSolved   = wordsSolved,
                        winPercentage = winPercentage,
                        currentPoints = currentPoints,
                        lastPlayedAt  = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
                    )
                )
            ).data
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UpdateProfile", "HTTP ${e.code()} - $errorBody")
            throw e
        }
    }

    /**
     * Reads the image from the device, converts it to a multipart request,
     * uploads it to Strapi's media library, and returns the full image URL.
     */
    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String {
        val contentResolver = context.contentResolver
        val inputStream     = contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")
        val mimeType  = contentResolver.getType(imageUri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png"  -> "png"
            "image/webp" -> "webp"
            else         -> "jpg"
        }
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val part        = MultipartBody.Part.createFormData("files", "avatar.$extension", requestBody)

        val relativePath = api.uploadAvatar(part).first().url
        return "http://192.168.0.100:1337$relativePath"
    }

    /** Fetches top [limit] profiles from Strapi sorted by currentPoints descending. */
    override suspend fun getLeaderboard(limit: Int): List<ProfileItem> {
        return api.getLeaderboard(limit = limit).data
    }
}