package com.khammin.game.data.remote.datasource.game

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.khammin.core.domain.model.GameRoom
import com.khammin.core.domain.model.PlayerState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val RESET_TAG    = "MultiplayerReset"

class MultiplayerDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rtdb: FirebaseDatabase,
    private val auth: FirebaseAuth,
) : MultiplayerDataSource {

    // ── Presence state tracking ───────────────────────────────────────────────
    // key = "roomId/userId"
    private val presenceStates      = ConcurrentHashMap<String, String>()
    private val connectedListeners  = ConcurrentHashMap<String, ValueEventListener>()
    private val disconnectedTimestamps = ConcurrentHashMap<String, Long>()

    // "guest_" prefix covers manually-assigned guest IDs; isAnonymous covers
    // Firebase anonymous auth users whose UIDs look like regular Firebase UIDs.
    private fun userType(userId: String): String {
        if (userId.startsWith("guest_")) return "guest"
        val current = auth.currentUser
        if (current != null && current.uid == userId && current.isAnonymous) return "guest"
        return "logged-in"
    }
    private fun ts() = System.currentTimeMillis()

    private val rooms = firestore.collection("rooms")

    override suspend fun createRoom(room: GameRoom): String {
        val start = System.currentTimeMillis()
        val doc = rooms.document()
        Log.d("RoomPerf", "[DataSource] createRoom → doc ID generated: ${doc.id} | step=${System.currentTimeMillis() - start}ms")
        val roomWithId = room.copy(roomId = doc.id)
        val setStart = System.currentTimeMillis()
        Log.d("RoomPerf", "[DataSource] createRoom → Firestore set() dispatched | roomId=${doc.id}")
        doc.set(roomWithId).await()
        Log.d("RoomPerf", "[DataSource] createRoom → Firestore set() ack | step=${System.currentTimeMillis() - setStart}ms | total=${System.currentTimeMillis() - start}ms | roomId=${doc.id}")
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

    override suspend fun restartRoom(roomId: String, newWord: String, wordLength: Int, roundNumber: Int, totalPoints: Map<String, Int>) {
        val start = System.currentTimeMillis()
        Log.d(RESET_TAG, "[Firestore] restartRoom → writing status='playing' | roomId=$roomId | word=$newWord | round=$roundNumber")
        rooms.document(roomId)
            .update(mapOf(
                "word"           to newWord,
                "wordLength"     to wordLength,
                "status"         to "playing",
                "winnerId"       to null,
                "playAgainVotes" to emptyList<String>(),
                "roundNumber"    to roundNumber,
                "totalPoints"    to totalPoints,
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

    override suspend fun registerPresence(roomId: String, userId: String) {
        val key = "$roomId/$userId"
        presenceStates[key] = "online"
        setupConnectedPresenceListener(key, roomId, userId)
    }

    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) {
        val key = "$roomId/$userId"
        val state = if (isForeground) "online" else "background"
        presenceStates[key] = state
        val label = if (isForeground) "ONLINE" else "OFFLINE"
        Log.d("PresenceDebug", "[$label] userType=${userType(userId)} | uid=$userId | time=${ts()}")
        if (isForeground) {
            // Reset the RTDB backoff state and force an immediate reconnect.
            // goOnline() alone is a no-op unless goOffline() was previously called; the
            // backoff timer keeps running. By calling goOffline() first we cancel the
            // pending retry, then goOnline() starts a fresh connect attempt with zero delay.
            // Must be called AFTER the caller has refreshed the auth token so RTDB
            // authenticates successfully on the very first reconnect attempt.
            Log.d("PresenceDebug", "[goOffline] userType=${userType(userId)} | uid=$userId | time=${ts()}")
            rtdb.goOffline()
            Log.d("PresenceDebug", "[goOnline] userType=${userType(userId)} | uid=$userId | time=${ts()}")
            rtdb.goOnline()
        }
        // Fire-and-forget — Firebase SDK queues the write and flushes when connected.
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
                    Log.d("PresenceDebug", "[.info/connected] state=disconnected | userType=${userType(userId)} | uid=$userId | time=${ts()}")
                    return
                }
                val disconnectedAt = disconnectedTimestamps.remove(key) ?: 0L
                val offlineDuration = if (disconnectedAt > 0L) ts() - disconnectedAt else 0L
                Log.d("PresenceDebug", "[.info/connected] state=connected | offlineDuration=${offlineDuration}ms | userType=${userType(userId)} | uid=$userId | time=${ts()}")
                val stateToWrite = presenceStates[key] ?: "online"
                // Re-register server-side disconnect hook first (Firebase docs order).
                userRef.onDisconnect().setValue("offline")
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
                val raw = snapshot.getValue(String::class.java)
                val isAfk = raw == "background" || raw == "offline"
                // Treat "offline" the same as "background" for the 😴 indicator.
                // On Android, the WebSocket is killed within seconds of going to background,
                // causing onDisconnect() to write "offline" before Firebase can reconnect.
                // Both states should show 😴 — the drop timer handles true removal.
                val label = if (isAfk) "OFFLINE" else "ONLINE"
                Log.d("PresenceDebug", "[isAfk→$label] raw='$raw' | userType=${userType(userId)} | uid=$userId | time=${ts()}")
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
            .update("status", "playing")
            .await()
    }

    override suspend fun resetCustomRoom(roomId: String) {
        rooms.document(roomId)
            .update(mapOf(
                "status"          to "waiting",
                "word"            to "",
                "wordLength"      to 0,
                "winnerId"        to null,
                "failedBy"        to "",
                "leftBy"          to "",
                "playAgainVotes"  to emptyList<String>(),
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
        // Write only this player's entry — safe when multiple players write concurrently
        // and does not overwrite other players' points.
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
                val raw = snapshot.getValue(String::class.java)
                // "online" and "background" = connected. "offline" / null = disconnected.
                val isConnected = raw == "online" || raw == "background"
                val label = if (isConnected) "ONLINE" else "OFFLINE"
                Log.d("PresenceDebug", "[presence→$label] raw='$raw' | userType=${userType(opponentId)} | uid=$opponentId | time=${ts()}")
                trySend(isConnected)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }
}