package com.khammin.game.data.remote.datasource.profile

import android.content.Context
import android.net.Uri
import com.khammin.game.data.remote.api.ProfileApiService
import com.khammin.game.data.remote.model.CreateProfileData
import com.khammin.game.data.remote.model.CreateProfileRequest
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.data.remote.model.UpdateProfileData
import com.khammin.game.data.remote.model.UpdateProfileRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
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
        Log.d("ProfilePerf", "getProfile query → GET /profiles?filters[firebaseUid][\$eq]=$firebaseUid")
        val response = api.getProfile(firebaseUid)
        Log.d("ProfilePerf", "getProfile raw response → count=${response.data.size} items=${
            response.data.map { "id=${it.id} docId=${it.documentId} uid=${it.firebaseUid} name=${it.name}" }
        }")
        val result = response.data.firstOrNull()
        if (result == null) Log.d("ProfilePerf", "getProfile → returned null (empty data list)")
        return result
    }

    /** Creates a new profile in Strapi with the user's Firebase UID and display name. */
    override suspend fun createProfile(firebaseUid: String, name: String): ProfileItem {
        return api.createProfile(
            CreateProfileRequest(CreateProfileData(firebaseUid, name))
        ).data
    }

    override suspend fun updateProfile(
        documentId: String,
        firebaseUid: String,
        name: String,
        avatarUrl: String?,
        language: String,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem {
        // Use firebaseUid to get current profile
        val current = getProfile(firebaseUid) ?: throw Exception("Profile not found")

        val now = ZonedDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

        val data = if (language == "ar") {
            UpdateProfileData(
                name            = name,
                avatarUrl       = avatarUrl,
                enGamesPlayed   = current.enGamesPlayed,
                enWordsSolved   = current.enWordsSolved,
                enWinPercentage = current.enWinPercentage,
                enCurrentPoints = current.enCurrentPoints,
                enLastPlayedAt  = current.enLastPlayedAt,
                arGamesPlayed   = gamesPlayed,
                arWordsSolved   = wordsSolved,
                arWinPercentage = winPercentage,
                arCurrentPoints = currentPoints,
                arLastPlayedAt  = now,
            )
        } else {
            UpdateProfileData(
                name            = name,
                avatarUrl       = avatarUrl,
                enGamesPlayed   = gamesPlayed,
                enWordsSolved   = wordsSolved,
                enWinPercentage = winPercentage,
                enCurrentPoints = currentPoints,
                enLastPlayedAt  = now,
                arGamesPlayed   = current.arGamesPlayed,
                arWordsSolved   = current.arWordsSolved,
                arWinPercentage = current.arWinPercentage,
                arCurrentPoints = current.arCurrentPoints,
                arLastPlayedAt  = current.arLastPlayedAt,
            )
        }

        return api.updateProfile(documentId, UpdateProfileRequest(data)).data
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
        return "https://khammin.com$relativePath"
    }

    override suspend fun syncProfile(
        documentId: String,
        firebaseUid: String,
        name: String,
        avatarUrl: String?,
        enGamesPlayed: Int,
        enWordsSolved: Int,
        enWinPercentage: Double,
        enCurrentPoints: Int,
        enLastPlayedAt: String?,
        arGamesPlayed: Int,
        arWordsSolved: Int,
        arWinPercentage: Double,
        arCurrentPoints: Int,
        arLastPlayedAt: String?,
    ): ProfileItem {
        val data = UpdateProfileData(
            name            = name,
            avatarUrl       = avatarUrl,
            enGamesPlayed   = enGamesPlayed,
            enWordsSolved   = enWordsSolved,
            enWinPercentage = enWinPercentage,
            enCurrentPoints = enCurrentPoints,
            enLastPlayedAt  = enLastPlayedAt,
            arGamesPlayed   = arGamesPlayed,
            arWordsSolved   = arWordsSolved,
            arWinPercentage = arWinPercentage,
            arCurrentPoints = arCurrentPoints,
            arLastPlayedAt  = arLastPlayedAt,
        )
        return api.updateProfile(documentId, UpdateProfileRequest(data)).data
    }

    /** Fetches top [limit] profiles from Strapi sorted by currentPoints descending. */
    override suspend fun getLeaderboard(limit: Int, language: String): List<ProfileItem> {
        val sort = if (language == "ar") "arCurrentPoints:desc" else "enCurrentPoints:desc"
        return api.getLeaderboard(limit = limit, sort = sort).data
    }
}