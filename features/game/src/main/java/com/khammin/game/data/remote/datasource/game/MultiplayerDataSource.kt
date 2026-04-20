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
    suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int, roundNumber: Int = 1, totalPoints: Map<String, Int> = emptyMap())
    suspend fun claimRestart(roomId: String)
    suspend fun registerPresence(roomId: String, userId: String)
    suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean)
    fun observeOpponentPresence(roomId: String, opponentId: String): Flow<Boolean>
    suspend fun addGuestToRoom(roomId: String, guestId: String)
    suspend fun removeGuestFromRoom(roomId: String, guestId: String)
    suspend fun startRoom(roomId: String)
    suspend fun resetCustomRoom(roomId: String)
    suspend fun votePlayAgain(roomId: String, userId: String)
    suspend fun unvotePlayAgain(roomId: String, userId: String)
    suspend fun updateGuestProfile(roomId: String, userId: String, name: String, avatarColor: Long?, avatarEmoji: String?)
    suspend fun updateSessionPoints(roomId: String, sessionPoints: Map<String, Int>)
}