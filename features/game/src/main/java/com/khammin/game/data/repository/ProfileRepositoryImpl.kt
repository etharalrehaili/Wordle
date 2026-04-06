package com.khammin.game.data.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.local.worker.ProfileSyncWorker
import com.khammin.game.data.mappers.toDomain
import com.khammin.game.data.mappers.toEntity
import com.khammin.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import java.io.IOException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource,
    private val db: AppDatabase,
    private val workManager: WorkManager,
) : ProfileRepository {

    /**
     * Cache-first strategy:
     * 1. Return the cached profile immediately if one exists.
     * 2. Attempt a remote refresh in the background to keep the cache fresh.
     * 3. If offline and no cache exists, return null.
     * 4. If there is a pending local update (pendingSync = true), skip the remote
     *    refresh so the user's unsaved changes are not overwritten.
     */
    override suspend fun getProfile(firebaseUid: String): Profile? {
        val cached = db.profileDao().getProfile(firebaseUid)

        // Don't overwrite local pending changes with stale server data
        if (cached?.pendingSync == true) return cached.toDomain()

        return try {
            val remoteProfile = remote.getProfile(firebaseUid)
                ?: return cached?.toDomain()
            db.profileDao().insertProfile(remoteProfile.toEntity())
            remoteProfile.toDomain()
        } catch (e: IOException) {
            cached?.toDomain()
        }
    }

    override suspend fun createProfile(firebaseUid: String, email: String): Profile {
        val profile = remote.createProfile(firebaseUid, email)
        db.profileDao().insertProfile(profile.toEntity())
        return profile.toDomain()
    }

    /**
     * Remote-first with offline fallback:
     * 1. Try to send the update to the server.
     * 2. On success, cache the server response (pendingSync = false).
     * 3. On IOException (no network), save the update locally with pendingSync = true
     *    and schedule [ProfileSyncWorker] to push it once connectivity returns.
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
        return try {
            val profile = remote.updateProfile(
                documentId, firebaseUid, name, avatarUrl,
                language, gamesPlayed, wordsSolved, winPercentage, currentPoints
            )
            db.profileDao().insertProfile(profile.toEntity())
            profile.toDomain()
        } catch (e: IOException) {
            // Build the updated profile from the cached record so that the
            // opposite language's stats are preserved intact.
            val cached = db.profileDao().getProfile(firebaseUid)
            val offline = if (language == "ar") {
                cached?.copy(
                    name                = name,
                    avatarUrl           = avatarUrl,
                    arGamesPlayed       = gamesPlayed,
                    arWordsSolved       = wordsSolved,
                    arWinPercentage     = winPercentage,
                    arCurrentPoints     = currentPoints,
                    pendingSync         = true,
                    pendingSyncLanguage = language,
                ) ?: com.khammin.game.data.local.entity.ProfileEntity(
                    firebaseUid         = firebaseUid,
                    documentId          = documentId,
                    name                = name,
                    avatarUrl           = avatarUrl,
                    enGamesPlayed       = 0,
                    enWordsSolved       = 0,
                    enWinPercentage     = 0.0,
                    enCurrentPoints     = 0,
                    arGamesPlayed       = gamesPlayed,
                    arWordsSolved       = wordsSolved,
                    arWinPercentage     = winPercentage,
                    arCurrentPoints     = currentPoints,
                    pendingSync         = true,
                    pendingSyncLanguage = language,
                )
            } else {
                cached?.copy(
                    name                = name,
                    avatarUrl           = avatarUrl,
                    enGamesPlayed       = gamesPlayed,
                    enWordsSolved       = wordsSolved,
                    enWinPercentage     = winPercentage,
                    enCurrentPoints     = currentPoints,
                    pendingSync         = true,
                    pendingSyncLanguage = language,
                ) ?: com.khammin.game.data.local.entity.ProfileEntity(
                    firebaseUid         = firebaseUid,
                    documentId          = documentId,
                    name                = name,
                    avatarUrl           = avatarUrl,
                    enGamesPlayed       = gamesPlayed,
                    enWordsSolved       = wordsSolved,
                    enWinPercentage     = winPercentage,
                    enCurrentPoints     = currentPoints,
                    arGamesPlayed       = 0,
                    arWordsSolved       = 0,
                    arWinPercentage     = 0.0,
                    arCurrentPoints     = 0,
                    pendingSync         = true,
                    pendingSyncLanguage = language,
                )
            }

            db.profileDao().insertProfile(offline)
            scheduleSyncWorker()
            offline.toDomain()
        }
    }

    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String =
        remote.uploadAvatar(imageUri, context)

    override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> =
        remote.getLeaderboard(limit, language).map { it.toDomain() }

    /**
     * Called by [ProfileSyncWorker] when the network becomes available.
     * Iterates over all records with pendingSync = true and pushes each to the server.
     */
    override suspend fun syncPendingUpdates() {
        val pending = db.profileDao().getPendingSyncProfiles()
        for (entity in pending) {
            val language     = entity.pendingSyncLanguage ?: "en"
            val gamesPlayed  = if (language == "ar") entity.arGamesPlayed  else entity.enGamesPlayed
            val wordsSolved  = if (language == "ar") entity.arWordsSolved  else entity.enWordsSolved
            val winPct       = if (language == "ar") entity.arWinPercentage else entity.enWinPercentage
            val points       = if (language == "ar") entity.arCurrentPoints else entity.enCurrentPoints

            val synced = remote.updateProfile(
                documentId    = entity.documentId,
                firebaseUid   = entity.firebaseUid,
                name          = entity.name,
                avatarUrl     = entity.avatarUrl,
                language      = language,
                gamesPlayed   = gamesPlayed,
                wordsSolved   = wordsSolved,
                winPercentage = winPct,
                currentPoints = points,
            )
            db.profileDao().insertProfile(synced.toEntity()) // clears pendingSync
        }
    }

    private fun scheduleSyncWorker() {
        val request = OneTimeWorkRequestBuilder<ProfileSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        // KEEP: if a sync is already queued, don't enqueue another one
        workManager.enqueueUniqueWork("profile_sync", ExistingWorkPolicy.KEEP, request)
    }
}
