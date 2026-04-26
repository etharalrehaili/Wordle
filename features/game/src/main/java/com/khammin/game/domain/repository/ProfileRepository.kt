package com.khammin.game.domain.repository

import android.content.Context
import android.net.Uri
import com.khammin.game.domain.model.Profile

/**
 * Repository interface for profile operations.
 * Abstracts data sources from the domain layer — consumers don't need to know
 * whether data comes from the remote API or local cache.
 */
interface ProfileRepository {
    /** Fetches a profile by Firebase UID. Returns null if no profile exists.
     *  Set [forceRefresh] to true to bypass the local cache and fetch from the server. */
    suspend fun getProfile(firebaseUid: String, forceRefresh: Boolean = false): Profile?

    /** Creates a new profile in Strapi using the user's Firebase UID and email. */
    suspend fun createProfile(firebaseUid: String, email: String): Profile

    /** Updates an existing profile's display info and game statistics. */
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
    ): Profile

    /** Uploads an avatar image to Strapi and returns the full URL of the uploaded file. */
    suspend fun uploadAvatar(imageUri: Uri, context: Context): String

    /** Fetches the top [limit] players sorted by points for the leaderboard. */
    suspend fun getLeaderboard(limit: Int, language: String): List<Profile>

    /**
     * Atomically adds [delta] points to arCurrentPoints by fetching the fresh
     * server value first, then writing back. Avoids stale-cache overwrites.
     */
    suspend fun addArPoints(firebaseUid: String, delta: Int): Profile

    /**
     * Pushes all locally-saved profile updates (pendingSync = true) to the server.
     * Called by [ProfileSyncWorker] once network connectivity is restored.
     */
    suspend fun syncPendingUpdates()
}