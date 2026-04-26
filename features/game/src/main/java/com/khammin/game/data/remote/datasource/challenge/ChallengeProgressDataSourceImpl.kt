package com.khammin.game.data.remote.datasource.challenge

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.khammin.game.domain.model.ChallengeSnapshot
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.model.UserChallenge
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChallengeProgressDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChallengeProgressDataSource {

    private fun docRef(uid: String) =
        firestore.collection("userChallenges").document(uid)

    override fun observeSnapshot(uid: String): Flow<ChallengeSnapshot> = callbackFlow {
        val listener = docRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChallengeDebug", "[DataSource] observeSnapshot listener error uid=$uid", error)
                trySend(ChallengeSnapshot())
                return@addSnapshotListener
            }
            Log.d("ChallengeDebug", "[DataSource] observeSnapshot listener fired uid=$uid snapshotNull=${snapshot == null}")
            trySend(snapshot?.toChallengeSnapshot() ?: ChallengeSnapshot())
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getSnapshot(uid: String): ChallengeSnapshot =
        try { docRef(uid).get().await().toChallengeSnapshot() }
        catch (_: Exception) { ChallengeSnapshot() }

    override suspend fun initializeIfNeeded(uid: String) {
        val ref = docRef(uid)
        val exists = try { ref.get().await().exists() } catch (_: Exception) { return }
        if (exists) return

        // Challenge entries are added lazily by EvaluateChallengesUseCase on first game over.
        val data = mapOf(
            "challenges"     to emptyMap<String, Any>(),
            "lastPlayedDate" to "",
        )
        try { ref.set(data).await() } catch (_: Exception) { /* best-effort */ }
    }

    override suspend fun saveSnapshot(uid: String, snapshot: ChallengeSnapshot) {
        val challengesMap = snapshot.challenges.mapValues { (_, c) ->
            mapOf("status" to c.status.name, "progress" to c.progress)
        }
        val data = mapOf(
            "challenges"     to challengesMap,
            "lastPlayedDate" to snapshot.lastPlayedDate,
        )
        Log.d("ChallengeDebug", "[DataSource] saveSnapshot uid=$uid writing ${challengesMap.size} challenges, sample=${challengesMap.entries.take(3).map { "${it.key}=${it.value}" }}")
        try {
            docRef(uid).set(data, SetOptions.merge()).await()
            Log.d("ChallengeDebug", "[DataSource] Firestore write completed for uid=$uid")
        } catch (e: Exception) {
            Log.e("ChallengeDebug", "[DataSource] Firestore write FAILED for uid=$uid", e)
        }
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private fun DocumentSnapshot.toChallengeSnapshot(): ChallengeSnapshot {
        Log.d("ChallengeDebug", "[DataSource] toChallengeSnapshot — docExists=${exists()} id=$id")
        val raw = get("challenges")
        Log.d("ChallengeDebug", "[DataSource] 'challenges' field type=${raw?.javaClass?.simpleName} value=$raw")
        val rawMap = (raw as? Map<*, *>) ?: emptyMap<Any, Any>()
        val challenges = rawMap.entries.mapNotNull { (key, value) ->
            val id = key as? String ?: return@mapNotNull null
            val entry = value as? Map<*, *> ?: return@mapNotNull null
            val statusStr = entry["status"] as? String ?: ChallengeStatus.AVAILABLE.name
            val progress = (entry["progress"] as? Long)?.toInt() ?: (entry["progress"] as? Int) ?: 0
            val status = runCatching { ChallengeStatus.valueOf(statusStr) }
                .getOrDefault(ChallengeStatus.AVAILABLE)
            id to UserChallenge(id, status, progress)
        }.toMap()

        Log.d("ChallengeDebug", "[DataSource] parsed ${challenges.size} challenges — non-available=${challenges.values.filter { it.status != ChallengeStatus.AVAILABLE }.map { "${it.id}=${it.status}" }}")
        return ChallengeSnapshot(
            challenges     = challenges,
            lastPlayedDate = getString("lastPlayedDate") ?: "",
        )
    }
}
