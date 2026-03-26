package com.khammin.game.data.remote.datasource.profile

import android.content.Context
import android.net.Uri
import com.khammin.game.data.remote.model.ProfileItem

/**
 * Interface for remote profile data operations.
 * Implemented by [ProfileRemoteDataSourceImpl] using Retrofit + Strapi API.
 */
interface ProfileRemoteDataSource {
    /** Fetches a profile from Strapi filtered by Firebase UID. Returns null if not found. */
    suspend fun getProfile(firebaseUid: String): ProfileItem?

    /** Creates a new profile document in Strapi. [name] is derived from the user's email. */
    suspend fun createProfile(firebaseUid: String, name: String): ProfileItem

    /** Updates an existing Strapi profile document by [documentId]. */
    suspend fun updateProfile(
        documentId: String,
        firebaseUid: String,
        name: String,
        avatarUrl: String?,
        language: String,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem

    /**
     * Uploads an avatar image to Strapi's media library.
     * Returns the full URL of the uploaded file.
     */
    suspend fun uploadAvatar(imageUri: Uri, context: Context): String

    /** Fetches the top [limit] profiles sorted by points descending. */
    suspend fun getLeaderboard(limit: Int, language: String): List<ProfileItem>
}