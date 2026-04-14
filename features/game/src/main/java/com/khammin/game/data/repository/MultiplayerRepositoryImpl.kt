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
    override suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int) =
        dataSource.restartRoom(roomId, newWord, wordLength)
    override suspend fun claimRestart(roomId: String) = dataSource.claimRestart(roomId)
    override suspend fun registerPresence(roomId: String, userId: String) =
        dataSource.registerPresence(roomId, userId)

    override fun observeOpponentPresence(roomId: String, opponentId: String) =
        dataSource.observeOpponentPresence(roomId, opponentId)
    override suspend fun addGuestToRoom(roomId: String, guestId: String) =
        dataSource.addGuestToRoom(roomId, guestId)
    override suspend fun removeGuestFromRoom(roomId: String, guestId: String) =
        dataSource.removeGuestFromRoom(roomId, guestId)
    override suspend fun startRoom(roomId: String) =
        dataSource.startRoom(roomId)
}