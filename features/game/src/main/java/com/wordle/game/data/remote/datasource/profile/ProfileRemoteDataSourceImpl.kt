package com.wordle.game.data.remote.datasource.profile

import android.content.Context
import android.net.Uri
import android.util.Log
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
        Log.d("ProfileAvatar", "updateProfile: documentId=$documentId avatarUrl=$avatarUrl")
        val updated = api.updateProfile(
            documentId,
            UpdateProfileRequest(
                UpdateProfileData(name, avatarUrl, gamesPlayed, wordsSolved, winPercentage, currentPoints)
            )
        ).data
        Log.d("ProfileAvatar", "updateProfile response: avatarUrl=${updated.avatarUrl}")
        return updated
    }

    /**
     * Reads the image from the device, converts it to a multipart request,
     * uploads it to Strapi's media library, and returns the full image URL.
     */
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
//        return "http://10.0.2.2:1337$relativePath"
        val fullUrl = "http://192.168.0.140:1337$relativePath"
        Log.d("ProfileAvatar", "uploadAvatar: imageUri=$imageUri relativePath=$relativePath fullUrl=$fullUrl")
        return fullUrl
    }

    /** Fetches top [limit] profiles from Strapi sorted by currentPoints descending. */
    override suspend fun getLeaderboard(limit: Int): List<ProfileItem> {
        return api.getLeaderboard(limit = limit).data
    }
}