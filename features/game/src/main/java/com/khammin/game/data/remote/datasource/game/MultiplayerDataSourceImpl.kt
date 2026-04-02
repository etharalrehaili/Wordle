package com.khammin.game.data.remote.datasource.game

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val RESET_TAG    = "MultiplayerReset"
private const val PRESENCE_TAG = "MultiplayerPresence"

class MultiplayerDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rtdb: FirebaseDatabase
) : MultiplayerDataSource {

    private val rooms = firestore.collection("rooms")

    // ── Presence ─────────────────────────────────────────────────────────────
    // Uses Firebase Realtime Database onDisconnect so the server automatically
    // marks the player offline if the app is force-killed.
    // Note: RTDB detects disconnection via TCP keepalive, which typically takes
    // 60–90 seconds on mobile networks before onDisconnect fires.

    private fun presenceRef(roomId: String, userId: String) =
        rtdb.getReference("presence/$roomId/$userId")

    override fun setPresence(roomId: String, userId: String) {
        val ref = presenceRef(roomId, userId)
        Log.d(PRESENCE_TAG, "[DataSource] setPresence — path=${ref.path} | userId=$userId")
        ref.onDisconnect().setValue(false)
            .addOnSuccessListener { Log.d(PRESENCE_TAG, "[DataSource] onDisconnect registered ✅ | userId=$userId") }
            .addOnFailureListener { e -> Log.e(PRESENCE_TAG, "[DataSource] onDisconnect register FAILED ❌ | userId=$userId | error=${e.message}") }
        ref.setValue(true)
            .addOnSuccessListener { Log.d(PRESENCE_TAG, "[DataSource] setValue(true) success ✅ | userId=$userId") }
            .addOnFailureListener { e -> Log.e(PRESENCE_TAG, "[DataSource] setValue(true) FAILED ❌ | userId=$userId | error=${e.message}") }
    }

    override fun clearPresence(roomId: String, userId: String) {
        val ref = presenceRef(roomId, userId)
        Log.d(PRESENCE_TAG, "[DataSource] clearPresence — path=${ref.path} | userId=$userId")
        ref.onDisconnect().cancel()
        ref.removeValue()
    }

    override fun observePresence(roomId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref = presenceRef(roomId, userId)
        Log.d(PRESENCE_TAG, "[DataSource] Attaching RTDB listener — path=${ref.path} | userId=$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) == true
                Log.d(PRESENCE_TAG, "[DataSource] onDataChange — path=${ref.path} | value=${snapshot.value} | isOnline=$isOnline")
                trySend(isOnline)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(PRESENCE_TAG, "[DataSource] onCancelled ❌ — path=${ref.path} | code=${error.code} | message=${error.message} | details=${error.details}")
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose {
            Log.d(PRESENCE_TAG, "[DataSource] Removing RTDB listener — path=${ref.path}")
            ref.removeEventListener(listener)
        }
    }

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
        val start = System.currentTimeMillis()
        Log.d(RESET_TAG, "[Firestore] restartRoom → writing status='playing' | roomId=$roomId | word=$newWord")
        rooms.document(roomId)
            .update(mapOf(
                "word"       to newWord,
                "wordLength" to wordLength,
                "status"     to "playing",
                "winnerId"   to null,
            ))
            .await()
        Log.d(RESET_TAG, "[Firestore] restartRoom ack received — round-trip=${System.currentTimeMillis() - start}ms")
    }

    override suspend fun claimRestart(roomId: String) {
        val start = System.currentTimeMillis()
        Log.d(RESET_TAG, "[Firestore] claimRestart → writing status='restarting' | roomId=$roomId")
        rooms.document(roomId)
            .update("status", "restarting")
            .await()
        Log.d(RESET_TAG, "[Firestore] claimRestart ack received — round-trip=${System.currentTimeMillis() - start}ms")
    }
}