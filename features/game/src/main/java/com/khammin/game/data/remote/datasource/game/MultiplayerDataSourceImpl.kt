package com.khammin.game.data.remote.datasource.game

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import com.khammin.core.domain.model.RoomStatus
import com.khammin.game.domain.model.PresenceStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
    // True once .info/connected has fired TRUE for a key (guards against RTDB startup FALSE flash).
    private val connectedOnce          = ConcurrentHashMap<String, Boolean>()
    // Keys for which the next FALSE is intentional (goOffline call) and must not set background.
    private val intentionalOfflineKeys = ConcurrentHashMap.newKeySet<String>()

    private fun userType(userId: String): String {
        if (userId.startsWith("guest_")) return "guest"
        val current = auth.currentUser
        if (current != null && current.uid == userId && current.isAnonymous) return "guest"
        return "logged-in"
    }

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
        // Search both waiting and playing rooms so players can join between rounds
        val statuses = listOf(RoomStatus.WAITING.value, RoomStatus.PLAYING.value)
        for (status in statuses) {
            val snapshot = rooms
                .whereEqualTo("status", status)
                .get()
                .await()
            val match = snapshot.documents
                .firstOrNull { it.id.startsWith(shortCode.lowercase(), ignoreCase = true) }
            if (match != null) return match.id
        }
        return null
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
        val key  = "$roomId/$userId"
        val path = "presence/$roomId/$userId"
        val ref  = rtdb.getReference(path)
        presenceStates[key] = PresenceStatus.ONLINE.value
        // Register onDisconnect FIRST — before setValue and before the .info/connected listener.
        // This guarantees the server has the handler the moment the write lands, even if the
        // connection closes before the .info/connected callback fires.
        runCatching { ref.onDisconnect().setValue(PresenceStatus.BACKGROUND.value) }
        // Register onDisconnect for heartbeat: server zeros it when TCP drops, making it
        // immediately stale so the host's heartbeat poller detects AFK fast.
        val heartbeatRef = rtdb.getReference("heartbeat/$roomId/$userId")
        runCatching { heartbeatRef.onDisconnect().setValue(0) }
        runCatching { ref.setValue(PresenceStatus.ONLINE.value) }
        setupConnectedPresenceListener(key, roomId, userId)
    }

    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) {
        val key   = "$roomId/$userId"
        val state = if (isForeground) PresenceStatus.ONLINE.value else PresenceStatus.BACKGROUND.value
        presenceStates[key] = state
        val path = "presence/$roomId/$userId"
        val ref  = rtdb.getReference(path)
        if (isForeground) {
            // Mark so the upcoming goOffline-triggered FALSE event is not treated as a real disconnect.
            intentionalOfflineKeys.add(key)
            rtdb.goOffline()
            rtdb.goOnline()
            // goOffline() cancels all server-side onDisconnect handlers. Re-register before
            // writing "online" so the server immediately has a handler — if internet drops
            // before the .info/connected listener fires, Firebase will still write "background".
            runCatching { ref.onDisconnect().setValue(PresenceStatus.BACKGROUND.value) }
            // goOffline() also cancels the heartbeat onDisconnect — re-register it.
            val heartbeatRef = rtdb.getReference("heartbeat/$roomId/$userId")
            runCatching { heartbeatRef.onDisconnect().setValue(0) }
        }
        runCatching { ref.setValue(state) }
    }

    override fun cleanupPresence(roomId: String, userId: String) {
        val key = "$roomId/$userId"
        connectedListeners.remove(key)?.let {
            rtdb.getReference(".info/connected").removeEventListener(it)
        }
        presenceStates.remove(key)
        connectedOnce.remove(key)
        intentionalOfflineKeys.remove(key)
    }

    private fun setupConnectedPresenceListener(key: String, roomId: String, userId: String) {
        val path         = "presence/$roomId/$userId"
        val userRef      = rtdb.getReference(path)
        val connectedRef = rtdb.getReference(".info/connected")

        connectedListeners.remove(key)?.let {
            connectedRef.removeEventListener(it)
        }
        // Fresh listener — reset "has been connected" guard so the startup FALSE flash is ignored.
        connectedOnce.remove(key)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) == true
                if (!isConnected) {
                    disconnectedTimestamps[key] = System.currentTimeMillis()
                    when {
                        intentionalOfflineKeys.remove(key) -> {
                            // This FALSE was triggered by goOffline() — not a real disconnect.
                        }
                        connectedOnce[key] == true -> {
                            // Real internet disconnect after a stable connection. Mark as background
                            // so the next reconnect writes 'background' and the host sees AFK fast.
                            presenceStates[key] = PresenceStatus.BACKGROUND.value
                        }
                        // else: RTDB startup flash — ignore.
                    }
                    return
                }
                connectedOnce[key] = true
                disconnectedTimestamps.remove(key)
                val stateToWrite = presenceStates[key] ?: PresenceStatus.ONLINE.value
                userRef.onDisconnect().setValue(PresenceStatus.BACKGROUND.value)
                userRef.setValue(stateToWrite)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        connectedListeners[key] = listener
        connectedRef.addValueEventListener(listener)
    }

    override suspend fun sendHeartbeat(roomId: String, userId: String) {
        runCatching { rtdb.getReference("heartbeat/$roomId/$userId").setValue(ServerValue.TIMESTAMP) }
    }

    override fun observeHeartbeatAfk(roomId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref      = rtdb.getReference("heartbeat/$roomId/$userId")
        var lastBeat: Long? = null

        val rtdbListener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = System.currentTimeMillis()
                if (!snapshot.exists()) {
                    // Node not created yet — player is still connecting; treat as fresh
                    lastBeat = now
                    trySend(false)
                    return
                }
                val beat  = snapshot.getValue(Long::class.java) ?: 0L
                lastBeat  = beat
                val isAfk = beat == 0L || (now - beat) > 30_000L
                trySend(isAfk)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Polling coroutine: re-evaluates staleness every 10 s even when no new RTDB event arrives.
        // This catches a sustained internet outage between beat writes.
        val pollJob = launch {
            while (true) {
                delay(10_000L)
                val lb    = lastBeat ?: continue   // skip until first RTDB event
                val now   = System.currentTimeMillis()
                val isAfk = lb == 0L || (now - lb) > 30_000L
                trySend(isAfk)
            }
        }

        awaitClose {
            ref.removeEventListener(rtdbListener)
            pollJob.cancel()
        }
    }

    override fun observeIsAfk(roomId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.getReference("presence/$roomId/$userId")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val raw   = snapshot.getValue(String::class.java)
                // null means presence node doesn't exist yet — treat as disconnected
                val isAfk = raw == null || raw == PresenceStatus.BACKGROUND.value || raw == PresenceStatus.OFFLINE.value
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
