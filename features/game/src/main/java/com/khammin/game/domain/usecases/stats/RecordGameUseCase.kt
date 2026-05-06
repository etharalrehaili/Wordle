package com.khammin.game.domain.usecases.stats

import com.google.firebase.auth.FirebaseAuth
import com.khammin.game.data.repository.LocalStatsRepository
import com.khammin.game.data.repository.RemoteStatsRepository
import javax.inject.Inject

class RecordGameUseCase @Inject constructor(
    private val local: LocalStatsRepository,
    private val remote: RemoteStatsRepository,
    private val auth: FirebaseAuth,
) {
    suspend operator fun invoke(language: String, isWin: Boolean) {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) {
            local.recordGame(language, isWin)
        } else {
            remote.recordGame(language, isWin)
        }
    }
}
