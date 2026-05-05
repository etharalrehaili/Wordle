package com.khammin.game.data.remote.datasource.challenge

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
                trySend(ChallengeSnapshot())
                return@addSnapshotListener
            }
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
        try { docRef(uid).set(data, SetOptions.merge()).await() } catch (_: Exception) { }
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private fun DocumentSnapshot.toChallengeSnapshot(): ChallengeSnapshot {
        val raw = get("challenges")
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

        return ChallengeSnapshot(
            challenges     = challenges,
            lastPlayedDate = getString("lastPlayedDate") ?: "",
        )
    }
}
