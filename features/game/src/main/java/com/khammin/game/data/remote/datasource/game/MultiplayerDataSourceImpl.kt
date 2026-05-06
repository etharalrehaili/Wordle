package com.khammin.game.data.remote.datasource.game

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.domain.model.RoomStatus
import com.khammin.game.domain.model.PresenceStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class MultiplayerDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rtdb: FirebaseDatabase,
    private val auth: FirebaseAuth,
) : MultiplayerDataSource {

    private val presenceStates         = ConcurrentHashMap<String, String>()
    private val connectedListeners     = ConcurrentHashMap<String, ValueEventListener>()
    private val disconnectedTimestamps = ConcurrentHashMap<String, Long>()

    private fun userType(userId: String): String {
        if (userId.startsWith("guest_")) return "guest"
        val current = auth.currentUser
        if (current != null && current.uid == userId && current.isAnonymous) return "guest"
        return "logged-in"
    }

    private fun ts() = System.currentTimeMillis()

    private val rooms = firestore.collection("rooms")

    override suspend fun createRoom(room: GameRoom): String {
        val doc = rooms.document()
        val roomWithId = room.copy(roomId = doc.id)
        doc.set(roomWithId).await()
        return doc.id
    }

    override suspend fun joinRoom(roomId: String, guestId: String) {
        rooms.document(roomId)
            .update(mapOf("guestId" to guestId, "status" to RoomStatus.PLAYING.value))
            .await()
    }

    override fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = rooms.document(roomId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(GameRoom::class.java))
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
                trySend(snapshot?.toObject(PlayerState::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun finishRoom(roomId: String, winnerId: String, failedBy: String) {
        rooms.document(roomId)
            .update(mapOf(
                "status"   to RoomStatus.FINISHED.value,
                "winnerId" to winnerId.takeIf { it.isNotEmpty() },
                "failedBy" to failedBy,
            ))
            .await()
    }

    override suspend fun findRoomByCode(shortCode: String): String? {
        val snapshot = rooms
            .whereEqualTo("status", RoomStatus.WAITING.value)
            .get()
            .await()
        return snapshot.documents
            .firstOrNull { it.id.startsWith(shortCode.lowercase(), ignoreCase = true) }
            ?.id
    }

    override suspend fun getRoom(roomId: String): GameRoom? =
        rooms.document(roomId).get().await().toObject(GameRoom::class.java)

    override suspend fun leaveRoom(roomId: String, loserId: String) {
        val room = rooms.document(roomId).get().await().toObject(GameRoom::class.java) ?: return
        val winnerId = if (room.hostId == loserId) room.guestId else room.hostId
        rooms.document(roomId)
            .update(mapOf(
                "status"   to RoomStatus.FINISHED.value,
                "winnerId" to winnerId,
                "leftBy"   to loserId,
            ))
            .await()
    }

    override suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int, roundNumber: Int, totalPoints: Map<String, Int>) {
        rooms.document(roomId)
            .update(mapOf(
                "word"           to newWord,
                "wordLength"     to wordLength,
                "status"         to RoomStatus.PLAYING.value,
                "winnerId"       to null,
                "playAgainVotes" to emptyList<String>(),
                "roundNumber"    to roundNumber,
                "totalPoints"    to totalPoints,
            ))
            .await()
    }

    override suspend fun claimRestart(roomId: String) {
        rooms.document(roomId)
            .update("status", RoomStatus.RESTARTING.value)
            .await()
    }

    override suspend fun registerPresence(roomId: String, userId: String) {
        val key = "$roomId/$userId"
        presenceStates[key] = PresenceStatus.ONLINE.value
        setupConnectedPresenceListener(key, roomId, userId)
    }

    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) {
        val key   = "$roomId/$userId"
        val state = if (isForeground) PresenceStatus.ONLINE.value else PresenceStatus.BACKGROUND.value
        presenceStates[key] = state
        if (isForeground) {
            rtdb.goOffline()
            rtdb.goOnline()
        }
        runCatching { rtdb.getReference("presence/$roomId/$userId").setValue(state) }
    }

    override fun cleanupPresence(roomId: String, userId: String) {
        val key = "$roomId/$userId"
        connectedListeners.remove(key)?.let {
            rtdb.getReference(".info/connected").removeEventListener(it)
        }
        presenceStates.remove(key)
    }

    private fun setupConnectedPresenceListener(key: String, roomId: String, userId: String) {
        val userRef      = rtdb.getReference("presence/$roomId/$userId")
        val connectedRef = rtdb.getReference(".info/connected")

        connectedListeners.remove(key)?.let { connectedRef.removeEventListener(it) }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) == true
                if (!isConnected) {
                    disconnectedTimestamps[key] = ts()
                    return
                }
                disconnectedTimestamps.remove(key)
                val stateToWrite = presenceStates[key] ?: PresenceStatus.ONLINE.value
                userRef.onDisconnect().setValue(PresenceStatus.OFFLINE.value)
                userRef.setValue(stateToWrite)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        connectedListeners[key] = listener
        connectedRef.addValueEventListener(listener)
    }

    override fun observeIsAfk(roomId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.getReference("presence/$roomId/$userId")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val raw   = snapshot.getValue(String::class.java)
                val isAfk = raw == PresenceStatus.BACKGROUND.value || raw == PresenceStatus.OFFLINE.value
                trySend(isAfk)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun addGuestToRoom(roomId: String, guestId: String) {
        rooms.document(roomId)
            .update("guestIds", FieldValue.arrayUnion(guestId))
            .await()
    }

    override suspend fun removeGuestFromRoom(roomId: String, guestId: String) {
        rooms.document(roomId)
            .update(mapOf(
                "guestIds"       to FieldValue.arrayRemove(guestId),
                "playAgainVotes" to FieldValue.arrayRemove(guestId),
            ))
            .await()
    }

    override suspend fun startRoom(roomId: String) {
        rooms.document(roomId)
            .update("status", RoomStatus.PLAYING.value)
            .await()
    }

    override suspend fun resetCustomRoom(roomId: String) {
        rooms.document(roomId)
            .update(mapOf(
                "status"         to RoomStatus.WAITING.value,
                "word"           to "",
                "wordLength"     to 0,
                "winnerId"       to null,
                "failedBy"       to "",
                "leftBy"         to "",
                "playAgainVotes" to emptyList<String>(),
            ))
            .await()
    }

    override suspend fun votePlayAgain(roomId: String, userId: String) {
        rooms.document(roomId)
            .update("playAgainVotes", FieldValue.arrayUnion(userId))
            .await()
    }

    override suspend fun unvotePlayAgain(roomId: String, userId: String) {
        rooms.document(roomId)
            .update("playAgainVotes", FieldValue.arrayRemove(userId))
            .await()
    }

    override suspend fun updateGuestProfile(roomId: String, userId: String, name: String, avatarColor: Long?, avatarEmoji: String?, avatarUrl: String?) {
        rooms.document(roomId)
            .update(mapOf(
                "guestProfiles.$userId.name"        to name,
                "guestProfiles.$userId.avatarColor" to (avatarColor?.toString() ?: ""),
                "guestProfiles.$userId.avatarEmoji" to (avatarEmoji ?: ""),
                "guestProfiles.$userId.avatarUrl"   to (avatarUrl ?: ""),
            ))
            .await()
    }

    override suspend fun setPlayerReady(roomId: String, userId: String, isReady: Boolean) {
        rooms.document(roomId)
            .update("guestProfiles.$userId.ready", if (isReady) "true" else "false")
            .await()
    }

    override suspend fun updateSessionPoints(roomId: String, sessionPoints: Map<String, Int>) {
        rooms.document(roomId)
            .update("sessionPoints", sessionPoints)
            .await()
    }

    override suspend fun updatePlayerSessionPoints(roomId: String, userId: String, pts: Int) {
        rooms.document(roomId)
            .update("sessionPoints.$userId", pts)
            .await()
    }

    override suspend fun setLobbyWinner(roomId: String, winnerId: String) {
        rooms.document(roomId)
            .update("winnerId", winnerId)
            .await()
    }

    override fun observeOpponentPresence(roomId: String, opponentId: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.getReference("presence/$roomId/$opponentId")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val raw         = snapshot.getValue(String::class.java)
                val isConnected = raw == PresenceStatus.ONLINE.value || raw == PresenceStatus.BACKGROUND.value
                trySend(isConnected)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }
}