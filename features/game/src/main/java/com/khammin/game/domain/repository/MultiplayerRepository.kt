package com.khammin.game.domain.repository

import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface MultiplayerRepository {
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
}