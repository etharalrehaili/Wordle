package com.khammin.game.data.repository

import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import com.khammin.game.data.remote.datasource.game.MultiplayerDataSource
import com.khammin.game.domain.repository.MultiplayerRepository
import javax.inject.Inject

class MultiplayerRepositoryImpl @Inject constructor(
    private val dataSource: MultiplayerDataSource
) : MultiplayerRepository {

    override suspend fun createRoom(room: GameRoom) = dataSource.createRoom(room)
    override suspend fun joinRoom(roomId: String, guestId: String) = dataSource.joinRoom(roomId, guestId)
    override fun observeRoom(roomId: String) = dataSource.observeRoom(roomId)
    override suspend fun updatePlayerState(roomId: String, userId: String, state: PlayerState) = dataSource.updatePlayerState(roomId, userId, state)
    override fun observeOpponent(roomId: String, opponentId: String) = dataSource.observeOpponent(roomId, opponentId)
    override suspend fun finishRoom(roomId: String, winnerId: String, failedBy: String) =
        dataSource.finishRoom(roomId, winnerId, failedBy)
    override suspend fun findRoomByCode(shortCode: String) =
        dataSource.findRoomByCode(shortCode)
    override suspend fun getRoom(roomId: String) = dataSource.getRoom(roomId)
    override suspend fun leaveRoom(roomId: String, loserId: String) =
        dataSource.leaveRoom(roomId, loserId)
    override suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int, roundNumber: Int, totalPoints: Map<String, Int>) =
        dataSource.restartRoom(roomId, newWord, wordLength, roundNumber, totalPoints)
    override suspend fun claimRestart(roomId: String) = dataSource.claimRestart(roomId)
    override suspend fun registerPresence(roomId: String, userId: String) =
        dataSource.registerPresence(roomId, userId)
    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) =
        dataSource.updatePresenceState(roomId, userId, isForeground)

    override fun observeOpponentPresence(roomId: String, opponentId: String) =
        dataSource.observeOpponentPresence(roomId, opponentId)
    override suspend fun addGuestToRoom(roomId: String, guestId: String) =
        dataSource.addGuestToRoom(roomId, guestId)
    override suspend fun removeGuestFromRoom(roomId: String, guestId: String) =
        dataSource.removeGuestFromRoom(roomId, guestId)
    override suspend fun startRoom(roomId: String) =
        dataSource.startRoom(roomId)
    override suspend fun resetCustomRoom(roomId: String) =
        dataSource.resetCustomRoom(roomId)
    override suspend fun votePlayAgain(roomId: String, userId: String) =
        dataSource.votePlayAgain(roomId, userId)
    override suspend fun unvotePlayAgain(roomId: String, userId: String) =
        dataSource.unvotePlayAgain(roomId, userId)
    override suspend fun updateGuestProfile(roomId: String, userId: String, name: String, avatarColor: Long?, avatarEmoji: String?) =
        dataSource.updateGuestProfile(roomId, userId, name, avatarColor, avatarEmoji)
    override suspend fun updateSessionPoints(roomId: String, sessionPoints: Map<String, Int>) =
        dataSource.updateSessionPoints(roomId, sessionPoints)
}