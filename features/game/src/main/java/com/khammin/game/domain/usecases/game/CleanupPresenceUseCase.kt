package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class CleanupPresenceUseCase @Inject constructor(
    private val repo: MultiplayerRepository,
) {
    operator fun invoke(roomId: String, userId: String) =
        repo.cleanupPresence(roomId, userId)
}
