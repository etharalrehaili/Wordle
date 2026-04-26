package com.khammin.game.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.StatsRepository
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteStatsRepository @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : StatsRepository {

    override suspend fun recordGame(language: String, isWin: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (user.isAnonymous) return

        val profile = when (val result = getProfileUseCase(user.uid)) {
            is Resource.Success -> result.data ?: return
            else -> return
        }

        val currentPlayed = profile.gamesPlayedForLanguage(language)
        val currentSolved = profile.wordsSolvedForLanguage(language)
        val currentPoints = profile.pointsForLanguage(language)

        val newPlayed = currentPlayed + 1
        val newSolved = if (isWin) currentSolved + 1 else currentSolved
        val winRate   = newSolved.toDouble() / newPlayed * 100.0

        updateProfileUseCase(
            documentId    = profile.documentId,
            firebaseUid   = user.uid,
            name          = profile.name,
            avatarUrl     = profile.avatarUrl,
            language      = language,
            gamesPlayed   = newPlayed,
            wordsSolved   = newSolved,
            winPercentage = winRate,
            currentPoints = currentPoints,
        )
    }
}
