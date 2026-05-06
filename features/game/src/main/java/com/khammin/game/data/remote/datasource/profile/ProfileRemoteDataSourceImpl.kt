package com.khammin.game.data.remote.datasource.profile

import android.content.ContentResolver
import android.net.Uri
import com.khammin.core.data.ndk.KeyManager
import com.khammin.game.data.remote.api.ProfileApiService
import com.khammin.game.data.remote.model.CreateProfileData
import com.khammin.game.data.remote.model.CreateProfileRequest
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.data.remote.model.UpdateProfileData
import com.khammin.game.data.remote.model.UpdateProfileRequest
import com.khammin.game.domain.model.ProfileUpdate
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val api: ProfileApiService,
    private val contentResolver: ContentResolver,
) : ProfileRemoteDataSource {

    override suspend fun getProfile(firebaseUid: String): ProfileItem? =
        api.getProfile(firebaseUid).data.firstOrNull()

    override suspend fun createProfile(firebaseUid: String, name: String): ProfileItem =
        api.createProfile(
            CreateProfileRequest(CreateProfileData(firebaseUid, name))
        ).data

    override suspend fun updateProfile(update: ProfileUpdate): ProfileItem {
        val current = getProfile(update.firebaseUid) ?: throw Exception("Profile not found")

        val now = ZonedDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

        // Only Arabic stats are modelled. When language == "ar", update stats and
        // lastPlayedAt. For any other language (e.g. a name/avatar-only save from
        // ProfileViewModel with language = "en"), preserve the existing ar stats so
        // they are not overwritten with the caller's default-zero values.
        val data = if (update.language == "ar") {
            UpdateProfileData(
                name            = update.name,
                avatarUrl       = update.avatarUrl,
                arGamesPlayed   = update.gamesPlayed,
                arWordsSolved   = update.wordsSolved,
                arWinPercentage = update.winPercentage,
                arCurrentPoints = update.currentPoints,
                arLastPlayedAt  = now,
            )
        } else {
            UpdateProfileData(
                name            = update.name,
                avatarUrl       = update.avatarUrl,
                arGamesPlayed   = current.arGamesPlayed,
                arWordsSolved   = current.arWordsSolved,
                arWinPercentage = current.arWinPercentage,
                arCurrentPoints = current.arCurrentPoints,
                arLastPlayedAt  = current.arLastPlayedAt,
            )
        }

        return api.updateProfile(update.documentId, UpdateProfileRequest(data)).data
    }

    override suspend fun uploadAvatar(imageUri: Uri): String {
        val inputStream = contentResolver.openInputStream(imageUri)
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
        return "${KeyManager.getBaseHost()}$relativePath"
    }

    override suspend fun syncProfile(data: ProfileSyncData): ProfileItem {
        val body = UpdateProfileData(
            name            = data.name,
            avatarUrl       = data.avatarUrl,
            arGamesPlayed   = data.arGamesPlayed,
            arWordsSolved   = data.arWordsSolved,
            arWinPercentage = data.arWinPercentage,
            arCurrentPoints = data.arCurrentPoints,
            arLastPlayedAt  = data.arLastPlayedAt,
        )
        return api.updateProfile(data.documentId, UpdateProfileRequest(body)).data
    }

    override suspend fun getLeaderboard(limit: Int, language: String): List<ProfileItem> {
        val sort = if (language == "ar") "arCurrentPoints:desc" else "enCurrentPoints:desc"
        return api.getLeaderboard(limit = limit, sort = sort).data
    }
}