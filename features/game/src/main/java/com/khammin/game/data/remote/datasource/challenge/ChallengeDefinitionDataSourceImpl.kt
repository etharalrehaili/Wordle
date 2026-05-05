package com.khammin.game.data.remote.datasource.challenge

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.khammin.game.domain.model.ChallengeConditionType
import com.khammin.game.domain.model.ChallengeDifficulty
import com.khammin.game.domain.model.RemoteChallengeDefinition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ChallengeDefinitionDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChallengeDefinitionDataSource {

    private val collection get() = firestore.collection("challengeDefinitions")

    private fun getCurrentWeekId(): String {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        return String.format(Locale.US, "%d-W%02d", year, week)
    }

    private fun weekQuery() = collection
        .whereEqualTo("weekId", getCurrentWeekId())
        .whereEqualTo("isActive", true)

    override fun observeDefinitions(): Flow<List<RemoteChallengeDefinition>> = callbackFlow {
        val currentWeek = getCurrentWeekId()
        Log.d("ChallengeDebug", "currentWeek=$currentWeek")
        val listener = weekQuery().addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChallengeDebug", "[DefDataSource] observeDefinitions error", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            val definitions = snapshot?.toDefinitions() ?: emptyList()
            Log.d("ChallengeDebug", "allDefinitions=$definitions")
            trySend(definitions)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getDefinitions(): List<RemoteChallengeDefinition> =
        try {
            val currentWeek = getCurrentWeekId()
            Log.d("ChallengeDebug", "currentWeek=$currentWeek")
            val definitions = weekQuery().get().await().toDefinitions()
            Log.d("ChallengeDebug", "allDefinitions=$definitions")
            definitions
        } catch (e: Exception) {
            Log.e("ChallengeDebug", "[DefDataSource] getDefinitions error", e)
            emptyList()
        }

    // ── Parsing ───────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun QuerySnapshot.toDefinitions(): List<RemoteChallengeDefinition> =
        documents.mapNotNull { doc ->
            runCatching {
                val id            = doc.getString("id") ?: doc.id
                val titleAr       = doc.getString("titleAr") ?: ""
                val titleEn       = doc.getString("titleEn") ?: ""
                val points        = (doc.getLong("points") ?: 0L).toInt()
                val target        = (doc.getLong("target") ?: 1L).toInt()
                val difficulty    = doc.getString("difficulty")
                    ?.let { runCatching { ChallengeDifficulty.valueOf(it) }.getOrNull() }
                    ?: ChallengeDifficulty.BEGINNER
                val conditionType = doc.getString("conditionType")
                    ?.let { runCatching { ChallengeConditionType.valueOf(it) }.getOrNull() }
                    ?: return@mapNotNull null
                val conditionParams = (doc.get("conditionParams") as? Map<String, Any>) ?: emptyMap()
                val iconName      = doc.getString("iconName") ?: "star"
                val isActive      = doc.getBoolean("isActive") ?: true

                RemoteChallengeDefinition(
                    id              = id,
                    titleAr         = titleAr,
                    titleEn         = titleEn,
                    points          = points,
                    target          = target,
                    difficulty      = difficulty,
                    conditionType   = conditionType,
                    conditionParams = conditionParams,
                    iconName        = iconName,
                    isActive        = isActive,
                )
            }.onFailure { e ->
                Log.e("ChallengeDebug", "[DefDataSource] failed to parse doc ${doc.id}", e)
            }.getOrNull()
        }
}
