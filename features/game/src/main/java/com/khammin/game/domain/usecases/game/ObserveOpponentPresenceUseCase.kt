package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class ObserveOpponentPresenceUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    operator fun invoke(roomId: String, opponentId: String) =
        repo.observeOpponentPresence(roomId, opponentId)
}