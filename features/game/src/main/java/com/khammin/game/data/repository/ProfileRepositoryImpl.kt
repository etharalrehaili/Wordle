package com.khammin.game.data.repository

import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.khammin.game.data.local.db.AppDatabase
import com.khammin.game.data.local.entity.ProfileEntity
import com.khammin.game.data.local.worker.ProfileSyncWorker
import com.khammin.game.data.mappers.toDomain
import com.khammin.game.data.mappers.toEntity
import com.khammin.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.khammin.game.data.remote.datasource.profile.ProfileSyncData
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.model.ProfileUpdate
import com.khammin.game.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource,
    private val db: AppDatabase,
    private val workManager: WorkManager,
) : ProfileRepository {

    override suspend fun getProfile(firebaseUid: String, forceRefresh: Boolean): Profile? {
        if (!forceRefresh) {
            val cached = db.profileDao().getProfile(firebaseUid)
            if (cached != null) return cached.toDomain()
        }

        return try {
            val remoteProfile = remote.getProfile(firebaseUid) ?: return null
            db.profileDao().insertProfile(remoteProfile.toEntity())
            remoteProfile.toDomain()
        } catch (e: Exception) {
            if (forceRefresh) db.profileDao().getProfile(firebaseUid)?.toDomain() else null
        }
    }

    override fun observeProfile(firebaseUid: String): Flow<Profile?> =
        db.profileDao().observeProfile(firebaseUid).map { it?.toDomain() }

    override suspend fun createProfile(firebaseUid: String, email: String): Profile {
        val profile = remote.createProfile(firebaseUid, email)
        db.profileDao().insertProfile(profile.toEntity())
        return profile.toDomain()
    }

    override suspend fun updateProfile(update: ProfileUpdate): Profile {
        return try {
            saveProfileItem(remote.updateProfile(update), update.firebaseUid)
        } catch (e: Exception) {
            val cached = db.profileDao().getProfile(update.firebaseUid)
            val offline = cached?.copy(
                name                = update.name,
                avatarUrl           = update.avatarUrl,
                arGamesPlayed       = update.gamesPlayed,
                arWordsSolved       = update.wordsSolved,
                arWinPercentage     = update.winPercentage,
                arCurrentPoints     = update.currentPoints,
                pendingSync         = true,
                pendingSyncLanguage = "ar",
            ) ?: ProfileEntity(
                firebaseUid         = update.firebaseUid,
                documentId          = update.documentId,
                name                = update.name,
                avatarUrl           = update.avatarUrl,
                arGamesPlayed       = update.gamesPlayed,
                arWordsSolved       = update.wordsSolved,
                arWinPercentage     = update.winPercentage,
                arCurrentPoints     = update.currentPoints,
                pendingSync         = true,
                pendingSyncLanguage = "ar",
            )

            db.profileDao().insertProfile(offline)
            scheduleSyncWorker()
            offline.toDomain()
        }
    }

    override suspend fun uploadAvatar(imageUri: Uri): String =
        remote.uploadAvatar(imageUri)

    override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> =
        remote.getLeaderboard(limit, language).map { it.toDomain() }

    override suspend fun addArPoints(firebaseUid: String, delta: Int): Profile {
        val current = remote.getProfile(firebaseUid) ?: throw Exception("Profile not found")
        val updated = remote.updateProfile(
            ProfileUpdate(
                documentId    = current.documentId,
                firebaseUid   = firebaseUid,
                name          = current.name,
                avatarUrl     = current.avatarUrl,
                language      = "ar",
                gamesPlayed   = current.arGamesPlayed,
                wordsSolved   = current.arWordsSolved,
                winPercentage = current.arWinPercentage,
                currentPoints = current.arCurrentPoints + delta,
            )
        )
        return saveProfileItem(updated, firebaseUid)
    }

    override suspend fun syncPendingUpdates() {
        val pending = db.profileDao().getPendingSyncProfiles()
        for (entity in pending) {
            val synced = remote.syncProfile(
                ProfileSyncData(
                    documentId      = entity.documentId,
                    firebaseUid     = entity.firebaseUid,
                    name            = entity.name,
                    avatarUrl       = entity.avatarUrl,
                    arGamesPlayed   = entity.arGamesPlayed,
                    arWordsSolved   = entity.arWordsSolved,
                    arWinPercentage = entity.arWinPercentage,
                    arCurrentPoints = entity.arCurrentPoints,
                    arLastPlayedAt  = entity.arLastPlayedAt,
                )
            )
            saveProfileItem(synced, entity.firebaseUid)
        }
    }

    private suspend fun saveProfileItem(item: ProfileItem, firebaseUid: String): Profile {
        val entity = item.toEntity().let {
            if (it.firebaseUid.isBlank()) it.copy(firebaseUid = firebaseUid) else it
        }
        db.profileDao().insertProfile(entity)
        return entity.toDomain()
    }

    private fun scheduleSyncWorker() {
        val request = OneTimeWorkRequestBuilder<ProfileSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork("profile_sync", ExistingWorkPolicy.KEEP, request)
    }
}