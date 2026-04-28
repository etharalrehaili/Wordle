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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource,
    private val db: AppDatabase,
    private val workManager: WorkManager,
) : ProfileRepository {

    /**
     * Cache-first strategy — local Room DB is the source of truth:
     * 1. If a cached profile exists (with or without a pending sync), return it
     *    immediately. The cache is always kept fresh by [updateProfile] and
     *    [syncPendingUpdates], so there is no need to re-fetch from the server
     *    on every screen open (which would risk overwriting fresh local data with
     *    a stale server response).
     * 2. Only go to the server when there is no cached profile at all (first
     *    launch or new device installation), then populate the cache from the
     *    server response.
     * 3. If offline and still no cache, return null.
     */
    override suspend fun getProfile(firebaseUid: String): Profile? {
        val cached = db.profileDao().getProfile(firebaseUid)
        if (cached != null) return cached.toDomain()

        // No local data — first launch or new device: fetch from server.
        return try {
            val remoteProfile = remote.getProfile(firebaseUid) ?: return null
            db.profileDao().insertProfile(remoteProfile.toEntity())
            remoteProfile.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun observeProfile(firebaseUid: String): Flow<Profile?> =
        db.profileDao().observeProfile(firebaseUid).map { it?.toDomain() }

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
            // Fix: Strapi's PUT response may omit or blank-out firebaseUid depending on
            // JSON field mapping. Always use the caller-supplied uid as the cache key so
            // the existing Room row is replaced rather than a new empty-uid row inserted.
            val entity = profile.toEntity().let {
                if (it.firebaseUid.isBlank()) it.copy(firebaseUid = firebaseUid) else it
            }
            db.profileDao().insertProfile(entity)
            entity.toDomain()
        } catch (e: Exception) {
            // Catch ALL exceptions (IOException for no-connectivity, HttpException for
            // server errors, plain Exception from remote.updateProfile's internal
            // getProfile call) so the update is never silently dropped.
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
            // Use syncProfile instead of updateProfile: the pending entity already has
            // both languages' stats fully merged (done at offline-save time), so we
            // don't need a round-trip getProfile call to fetch the "other language" stats.
            val synced = remote.syncProfile(
                documentId      = entity.documentId,
                firebaseUid     = entity.firebaseUid,
                name            = entity.name,
                avatarUrl       = entity.avatarUrl,
                enGamesPlayed   = entity.enGamesPlayed,
                enWordsSolved   = entity.enWordsSolved,
                enWinPercentage = entity.enWinPercentage,
                enCurrentPoints = entity.enCurrentPoints,
                enLastPlayedAt  = entity.enLastPlayedAt,
                arGamesPlayed   = entity.arGamesPlayed,
                arWordsSolved   = entity.arWordsSolved,
                arWinPercentage = entity.arWinPercentage,
                arCurrentPoints = entity.arCurrentPoints,
                arLastPlayedAt  = entity.arLastPlayedAt,
            )
            // Always use known firebaseUid as cache key (same safety as updateProfile above)
            db.profileDao().insertProfile(
                synced.toEntity().let {
                    if (it.firebaseUid.isBlank()) it.copy(firebaseUid = entity.firebaseUid) else it
                }
            )
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
