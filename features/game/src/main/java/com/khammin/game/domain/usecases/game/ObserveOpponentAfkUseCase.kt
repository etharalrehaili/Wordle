package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class ObserveOpponentAfkUseCase @Inject constructor(
    private val repo: MultiplayerRepository,
) {
    operator fun invoke(roomId: String, userId: String) =
        repo.observeIsAfk(roomId, userId)
}
