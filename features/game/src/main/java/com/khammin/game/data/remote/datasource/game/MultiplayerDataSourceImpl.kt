package com.khammin.game.data.remote.datasource.game

import android.util.Log
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
import javax.inject.Inject

private const val RESET_TAG    = "MultiplayerReset"

class MultiplayerDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rtdb: FirebaseDatabase
) : MultiplayerDataSource {

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
        Log.d("Presence", "registerPresence called | userId=$userId | roomId=$roomId")
        val ref = rtdb.getReference("presence/$roomId/$userId")
        try {
            ref.setValue("online").await()
            Log.d("Presence", "Presence set to 'online' | userId=$userId")
            // Hard disconnect (process kill, network loss) → write "offline" instead of
            // removing the node so observers can distinguish offline from never-connected.
            ref.onDisconnect().setValue("offline").await()
            Log.d("Presence", "onDisconnect registered for $userId")
        } catch (e: Exception) {
            Log.e("Presence", "registerPresence failed | userId=$userId | roomId=$roomId", e)
        }
    }

    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) {
        val ref = rtdb.getReference("presence/$roomId/$userId")
        try {
            if (isForeground) {
                ref.setValue("online").await()
                Log.d("Presence", "Presence set to 'online' | userId=$userId")
                // Re-register onDisconnect so the hook is always current after a reconnect.
                ref.onDisconnect().setValue("offline").await()
                Log.d("Presence", "onDisconnect registered for $userId")
            } else {
                ref.setValue("background").await()
                Log.d("Presence", "Presence set to 'background' | userId=$userId")
                // Keep onDisconnect() pointing to "offline" so a hard-kill while backgrounded
                // still produces a clean "offline" value rather than leaving "background" forever.
                ref.onDisconnect().setValue("offline").await()
                Log.d("Presence", "onDisconnect registered for $userId")
            }
        } catch (e: Exception) {
            Log.e("Presence", "updatePresenceState failed | userId=$userId | isForeground=$isForeground", e)
        }
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
                Log.d("Presence", "observeOpponentPresence value changed | opponentId=$opponentId | raw='$raw'")
                // "online" and "background" are both considered connected.
                // "offline" and null (node absent / hard disconnect) are disconnected.
                val isConnected = raw == "online" || raw == "background"
                if (isConnected) {
                    Log.d("Presence", "Opponent $opponentId connected - raw value: $raw")
                } else {
                    Log.d("Presence", "Opponent $opponentId disconnected - raw value: $raw")
                }
                trySend(isConnected)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Presence", "observeOpponentPresence listener cancelled | opponentId=$opponentId | error=${error.message}")
            }
        })
        awaitClose {
            Log.d("Presence", "observeOpponentPresence cleanup - removing listener | opponentId=$opponentId")
            ref.removeEventListener(listener)
        }
    }
}