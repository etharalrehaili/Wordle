package com.khammin.game.domain.usecases.stats

import com.google.firebase.auth.FirebaseAuth
import com.khammin.game.data.repository.LocalStatsRepository
import com.khammin.game.data.repository.RemoteStatsRepository
import javax.inject.Inject

/**
 * Records a completed game result into the correct stats store based on auth state.
 * - Anonymous users  → local SharedPreferences via [LocalStatsRepository]
 * - Google users     → Strapi profile via [RemoteStatsRepository]
 */
class RecordGameUseCase @Inject constructor(
    private val local: LocalStatsRepository,
    private val remote: RemoteStatsRepository,
) {
    suspend operator fun invoke(language: String, isWin: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (user.isAnonymous) {
            local.recordGame(language, isWin)
        } else {
            remote.recordGame(language, isWin)
        }
    }
}
