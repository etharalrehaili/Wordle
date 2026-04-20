package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class VotePlayAgainUseCase @Inject constructor(
    private val repo: MultiplayerRepository
) {
    suspend fun vote(roomId: String, userId: String) = repo.votePlayAgain(roomId, userId)
    suspend fun unvote(roomId: String, userId: String) = repo.unvotePlayAgain(roomId, userId)
}
