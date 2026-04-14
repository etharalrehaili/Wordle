package com.khammin.game.data.remote.datasource.game

import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface MultiplayerDataSource {
    suspend fun createRoom(room: GameRoom): String
    suspend fun joinRoom(roomId: String, guestId: String)
    fun observeRoom(roomId: String): Flow<GameRoom?>
    suspend fun updatePlayerState(roomId: String, userId: String, state: PlayerState)
    fun observeOpponent(roomId: String, opponentId: String): Flow<PlayerState?>
    suspend fun finishRoom(roomId: String, winnerId: String, failedBy: String = "")
    suspend fun findRoomByCode(shortCode: String): String?
    suspend fun getRoom(roomId: String): GameRoom?
    suspend fun leaveRoom(roomId: String, loserId: String)
    suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int)
    suspend fun claimRestart(roomId: String)
    suspend fun registerPresence(roomId: String, userId: String)
    fun observeOpponentPresence(roomId: String, opponentId: String): Flow<Boolean>
    suspend fun addGuestToRoom(roomId: String, guestId: String)
    suspend fun removeGuestFromRoom(roomId: String, guestId: String)
    suspend fun startRoom(roomId: String)
}