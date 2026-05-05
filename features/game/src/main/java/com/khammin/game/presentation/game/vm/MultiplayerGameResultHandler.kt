package com.khammin.game.presentation.game.vm

import com.khammin.game.domain.model.GameResult
import com.khammin.game.domain.usecases.challenges.AwardChallengePointsUseCase
import com.khammin.game.domain.usecases.challenges.EvaluateChallengesUseCase
import com.khammin.game.domain.usecases.game.SetLobbyWinnerUseCase
import com.khammin.game.domain.usecases.game.UpdatePlayerSessionPointsUseCase
import com.khammin.game.domain.usecases.game.UpdateSessionPointsUseCase
import com.khammin.game.domain.usecases.game.VotePlayAgainUseCase
import javax.inject.Inject

class MultiplayerGameResultHandler @Inject constructor(
    private val evaluateChallengesUseCase: EvaluateChallengesUseCase,
    private val awardChallengePointsUseCase: AwardChallengePointsUseCase,
    private val updateSessionPointsUseCase: UpdateSessionPointsUseCase,
    private val updatePlayerSessionPointsUseCase: UpdatePlayerSessionPointsUseCase,
    private val setLobbyWinnerUseCase: SetLobbyWinnerUseCase,
    private val votePlayAgainUseCase: VotePlayAgainUseCase,
) {
    suspend fun evaluateAndAward(gameResult: GameResult) {
        runCatching {
            val completed = evaluateChallengesUseCase(gameResult)
            awardChallengePointsUseCase(completed)
        }
    }

    suspend fun updateSessionPoints(roomId: String, points: Map<String, Int>) {
        runCatching { updateSessionPointsUseCase(roomId, points) }
    }

    suspend fun updatePlayerPoints(roomId: String, userId: String, pts: Int) {
        runCatching { updatePlayerSessionPointsUseCase(roomId, userId, pts) }
    }

    suspend fun setLobbyWinner(roomId: String, userId: String) {
        runCatching { setLobbyWinnerUseCase(roomId, userId) }
    }

    suspend fun vote(roomId: String, userId: String) {
        runCatching { votePlayAgainUseCase.vote(roomId, userId) }
    }

    suspend fun unvote(roomId: String, userId: String) {
        runCatching { votePlayAgainUseCase.unvote(roomId, userId) }
    }
}
