package com.khammin.game.data.local.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.khammin.game.domain.repository.ProfileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that pushes locally-saved profile updates to the server.
 * Scheduled by [ProfileRepositoryImpl] whenever an update is saved offline.
 * WorkManager automatically waits for [NetworkType.CONNECTED] before running.
 */
@HiltWorker
class ProfileSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val profileRepository: ProfileRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            profileRepository.syncPendingUpdates()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
