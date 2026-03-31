package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class RestartRoomUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend fun claimRestart(roomId: String) = repo.claimRestart(roomId)

    suspend operator fun invoke(roomId: String, newWord: String, wordLength: Int) =
        repo.restartRoom(roomId, newWord, wordLength)
}