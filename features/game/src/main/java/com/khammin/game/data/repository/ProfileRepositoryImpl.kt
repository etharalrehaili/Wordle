package com.khammin.game.data.repository

import android.content.Context
import android.net.Uri
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.mappers.toDomain
import com.khammin.game.data.mappers.toEntity
import com.khammin.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
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
//    override suspend fun getProfile(firebaseUid: String): Profile? {
//        val cached = db.profileDao().getProfile(firebaseUid)
//        if (cached != null) return cached.toDomain()
//
//        val remote = remote.getProfile(firebaseUid) ?: return null
//        db.profileDao().insertProfile(remote.toEntity())
//        return remote.toDomain()
//    }

    // ProfileRepositoryImpl — add this
    override suspend fun getProfile(firebaseUid: String): Profile? {
        val remote = remote.getProfile(firebaseUid) ?: return null
        db.profileDao().insertProfile(remote.toEntity())
        return remote.toDomain()
    }

    /**
     * Creates a new profile remotely and caches it locally.
     * Called once after a user registers for the first time.
     */
    override suspend fun createProfile(firebaseUid: String, email: String): Profile {
        val profile = remote.createProfile(firebaseUid, email)
        db.profileDao().insertProfile(profile.toEntity())
        return profile.toDomain()
    }

    /**
     * Updates the profile remotely and syncs the updated data to local cache.
     * Called after editing profile info or when game stats change.
     */
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
    ): Profile {
        val profile = remote.updateProfile(
            documentId, firebaseUid, name, avatarUrl,
            language, gamesPlayed, wordsSolved, winPercentage, currentPoints
        )
        db.profileDao().insertProfile(profile.toEntity())
        return profile.toDomain()
    }

    /** Delegates avatar upload entirely to the remote data source. */
    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String {
        return remote.uploadAvatar(imageUri, context)
    }

    /** Fetches leaderboard directly from API — not cached since it changes frequently. */
    override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> {
            return remote.getLeaderboard(limit, language).map { it.toDomain() }
    }
}