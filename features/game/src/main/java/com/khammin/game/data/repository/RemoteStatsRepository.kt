package com.khammin.game.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.util.Resource
import com.khammin.game.domain.repository.StatsRepository
import com.khammin.game.domain.model.ProfileUpdate
import com.khammin.game.domain.model.winPercentage
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteStatsRepository @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val auth: FirebaseAuth,
) : StatsRepository {

    override suspend fun recordGame(language: String, isWin: Boolean) {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return

        val profile = when (val result = getProfileUseCase(user.uid)) {
            is Resource.Success -> result.data ?: return
            else -> return
        }

        // Only Arabic stats are stored in the current data model.
        val currentPlayed = profile.arGamesPlayed
        val currentSolved = profile.arWordsSolved
        val currentPoints = profile.arCurrentPoints

        val newPlayed = currentPlayed + 1
        val newSolved = if (isWin) currentSolved + 1 else currentSolved
        val winRate   = winPercentage(newPlayed, newSolved)

        updateProfileUseCase(
            ProfileUpdate(
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
        )
    }
}
