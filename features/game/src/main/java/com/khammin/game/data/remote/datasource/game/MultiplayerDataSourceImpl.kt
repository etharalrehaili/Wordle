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
        val presenceRef = rtdb.getReference("presence/$roomId/$userId")
        Log.d("PresenceDebug", "[registerPresence] Setting value='online' | roomId=$roomId | userId=$userId")
        presenceRef.setValue("online").await()
        Log.d("PresenceDebug", "[registerPresence] Registering onDisconnect().removeValue() | roomId=$roomId | userId=$userId")
        presenceRef.onDisconnect().removeValue().await()
        Log.d("PresenceDebug", "[registerPresence] onDisconnect registered ✓ | roomId=$roomId | userId=$userId")
    }

    override suspend fun updatePresenceState(roomId: String, userId: String, isForeground: Boolean) {
        val ref = rtdb.getReference("presence/$roomId/$userId")
        if (isForeground) {
            Log.d("PresenceDebug", "[updatePresenceState] App FOREGROUND → writing 'online' + re-registering onDisconnect | roomId=$roomId | userId=$userId")
            ref.setValue("online").await()
            ref.onDisconnect().removeValue().await()
            Log.d("PresenceDebug", "[updatePresenceState] Foreground setup complete ✓ | roomId=$roomId | userId=$userId")
        } else {
            // Do NOT cancel or re-register onDisconnect here.
            // The hook registered in registerPresence stays active so that if the app
            // is killed while in the background, Firebase still removes the node.
            // Brief null spikes from connection drops are absorbed by the grace period
            // in the ViewModel observer.
            Log.d("PresenceDebug", "[updatePresenceState] App BACKGROUND → writing 'background' (onDisconnect hook unchanged) | roomId=$roomId | userId=$userId")
            ref.setValue("background").await()
            Log.d("PresenceDebug", "[updatePresenceState] Background setup complete ✓ | roomId=$roomId | userId=$userId")
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

    override suspend fun updateGuestProfile(roomId: String, userId: String, name: String, avatarColor: Long?, avatarEmoji: String?) {
        rooms.document(roomId)
            .update("guestProfiles.$userId", mapOf(
                "name"        to name,
                "avatarColor" to (avatarColor?.toString() ?: ""),
                "avatarEmoji" to (avatarEmoji ?: ""),
            ))
            .await()
    }

    override suspend fun updateSessionPoints(roomId: String, sessionPoints: Map<String, Int>) {
        rooms.document(roomId)
            .update("sessionPoints", sessionPoints)
            .await()
    }

    override fun observeOpponentPresence(roomId: String, opponentId: String): Flow<Boolean> = callbackFlow {
        Log.d("PresenceDebug", "[observeOpponentPresence] Attaching listener | roomId=$roomId | opponentId=$opponentId")
        val ref = rtdb.getReference("presence/$roomId/$opponentId")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val raw = snapshot.getValue(String::class.java)
                val isPresent = raw != null
                Log.d("PresenceDebug", "[observeOpponentPresence] Value changed → raw='$raw' | isPresent=$isPresent | roomId=$roomId | opponentId=$opponentId")
                trySend(isPresent)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("PresenceDebug", "[observeOpponentPresence] Listener cancelled | error=${error.message} | roomId=$roomId | opponentId=$opponentId")
            }
        })
        awaitClose {
            Log.d("PresenceDebug", "[observeOpponentPresence] Removing listener | roomId=$roomId | opponentId=$opponentId")
            ref.removeEventListener(listener)
        }
    }
}