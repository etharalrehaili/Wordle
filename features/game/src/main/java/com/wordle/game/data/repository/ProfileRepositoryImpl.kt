package com.wordle.game.data.repository

import android.content.Context
import android.net.Uri
import com.wordle.game.data.local.db.AppDatabase
import com.wordle.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Implementation of [ProfileRepository].
 * Uses a cache-first strategy:
 * - For reads: checks Room DB first, falls back to remote API if not cached.
 * - For writes: updates remote API first, then syncs the result to local DB.
 */
class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource,
    private val db: AppDatabase,
) : ProfileRepository {

    /**
     * Returns cached profile from Room if available.
     * Otherwise fetches from API, caches the result, and returns it.
     */
    override suspend fun getProfile(firebaseUid: String): ProfileItem? {
        val cached = db.profileDao().getProfile(firebaseUid)
        if (cached != null) return cached.toProfileItem()

        val remote = remote.getProfile(firebaseUid) ?: return null
        db.profileDao().insertProfile(remote.toEntity())
        return remote
    }

    /**
     * Creates a new profile remotely and caches it locally.
     * Called once after a user registers for the first time.
     */
    override suspend fun createProfile(firebaseUid: String, email: String): ProfileItem {
        val profile = remote.createProfile(firebaseUid, email)
        db.profileDao().insertProfile(profile.toEntity())
        return profile
    }

    /**
     * Updates the profile remotely and syncs the updated data to local cache.
     * Called after editing profile info or when game stats change.
     */
    override suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem {
        val profile = remote.updateProfile(
            documentId, name, avatarUrl, gamesPlayed, wordsSolved, winPercentage, currentPoints
        )
        db.profileDao().insertProfile(profile.toEntity())
        return profile
    }

    /** Delegates avatar upload entirely to the remote data source. */
    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String {
        return remote.uploadAvatar(imageUri, context)
    }

    /** Fetches leaderboard directly from API — not cached since it changes frequently. */
    override suspend fun getLeaderboard(limit: Int): List<ProfileItem> {
        return remote.getLeaderboard(limit)
    }
}