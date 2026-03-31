package com.khammin.game.data.remote.datasource.game

import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MultiplayerDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MultiplayerDataSource {

    private val rooms = firestore.collection("rooms")

    override suspend fun createRoom(room: GameRoom): String {
        val doc = rooms.document()
        val roomWithId = room.copy(roomId = doc.id)
        doc.set(roomWithId).await()
        return doc.id
    }

    override suspend fun joinRoom(roomId: String, guestId: String) {
        rooms.document(roomId)
            .update(mapOf("guestId" to guestId, "status" to "playing"))
            .await()
    }

    override fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = rooms.document(roomId)
            .addSnapshotListener { snapshot, _ ->
                val room = snapshot?.toObject(GameRoom::class.java)
                trySend(room)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updatePlayerState(roomId: String, userId: String, state: PlayerState) {
        rooms.document(roomId)
            .collection("players")
            .document(userId)
            .set(state)
            .await()
    }

    override fun observeOpponent(roomId: String, opponentId: String): Flow<PlayerState?> = callbackFlow {
        val listener = rooms.document(roomId)
            .collection("players")
            .document(opponentId)
            .addSnapshotListener { snapshot, _ ->
                val state = snapshot?.toObject(PlayerState::class.java)
                trySend(state)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun finishRoom(roomId: String, winnerId: String, failedBy: String) {
        rooms.document(roomId)
            .update(mapOf(
                "status"   to "finished",
                "winnerId" to winnerId.takeIf { it.isNotEmpty() },
                "failedBy" to failedBy
            ))
            .await()
    }

    override suspend fun findRoomByCode(shortCode: String): String? {
        val snapshot = rooms
            .whereEqualTo("status", "waiting")
            .get()
            .await()
        return snapshot.documents
            .firstOrNull { it.id.startsWith(shortCode.lowercase(), ignoreCase = true) }
            ?.id
    }

    override suspend fun getRoom(roomId: String): GameRoom? =
        rooms.document(roomId).get().await().toObject(GameRoom::class.java)

    override suspend fun leaveRoom(roomId: String, loserId: String) {
        // We need to know who the opponent is to set them as winner
        val room = rooms.document(roomId).get().await().toObject(GameRoom::class.java) ?: return
        val winnerId = if (room.hostId == loserId) room.guestId else room.hostId
        rooms.document(roomId)
            .update(mapOf(
                "status"   to "finished",
                "winnerId" to winnerId,
                "leftBy"   to loserId
            ))
            .await()
    }

    override suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int) {
        rooms.document(roomId)
            .update(mapOf(
                "word"       to newWord,
                "wordLength" to wordLength,
                "status"     to "playing",
                "winnerId"   to null,
            ))
            .await()

    }
    override suspend fun claimRestart(roomId: String) {
        rooms.document(roomId)
            .update("status", "restarting")
            .await()
    }
}