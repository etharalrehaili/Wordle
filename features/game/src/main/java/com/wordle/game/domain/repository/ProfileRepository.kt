package com.wordle.game.domain.repository

import android.content.Context
import android.net.Uri
import com.wordle.game.domain.model.Profile

/**
 * Repository interface for profile operations.
 * Abstracts data sources from the domain layer — consumers don't need to know
 * whether data comes from the remote API or local cache.
 */
interface ProfileRepository {
    /** Fetches a profile by Firebase UID. Returns null if no profile exists. */
    suspend fun getProfile(firebaseUid: String): Profile?

    /** Creates a new profile in Strapi using the user's Firebase UID and email. */
    suspend fun createProfile(firebaseUid: String, email: String): Profile

    /** Updates an existing profile's display info and game statistics. */
    suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): Profile

    /** Uploads an avatar image to Strapi and returns the full URL of the uploaded file. */
    suspend fun uploadAvatar(imageUri: Uri, context: Context): String

    /** Fetches the top [limit] players sorted by points for the leaderboard. */
    suspend fun getLeaderboard(limit: Int): List<Profile>
}