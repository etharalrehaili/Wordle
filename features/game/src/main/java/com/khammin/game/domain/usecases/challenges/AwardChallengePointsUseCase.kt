package com.khammin.game.domain.usecases.challenges

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.perf.FirebasePerformance
import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import com.khammin.game.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Translates newly-completed challenge IDs into points.
 *
 * - Google users  → [ProfileRepository.addArPoints] (Strapi, server-fresh read)
 * - Anonymous users → [LocalStatsDataStore.addPoints] (SharedPreferences `stats_total_points`)
 *
 * Call immediately after [EvaluateChallengesUseCase] returns a non-empty list.
 */
class AwardChallengePointsUseCase @Inject constructor(
    private val definitionRepository: ChallengeDefinitionRepository,
    private val profileRepository: ProfileRepository,
    private val localStatsDataStore: LocalStatsDataStore,
) {
    suspend operator fun invoke(completedIds: List<String>) {
        if (completedIds.isEmpty()) return

        val trace = FirebasePerformance.getInstance().newTrace("award_challenge_points")
        trace.start()
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Log.w("ChallengeDebug", "[AwardPoints] user is null — skipping")
                return
            }

            val definitions  = definitionRepository.getDefinitions()
            val pointsEarned = definitions
                .filter { it.id in completedIds }
                .sumOf { it.points }

            trace.putAttribute("user_type", if (user.isAnonymous) "guest" else "google")
            trace.putMetric("points_earned", pointsEarned.toLong())

            Log.d("ChallengeDebug", "[AwardPoints] completedIds=$completedIds pointsEarned=$pointsEarned isAnonymous=${user.isAnonymous}")
            if (pointsEarned <= 0) return

            if (user.isAnonymous) {
                localStatsDataStore.addPoints(pointsEarned)
                Log.d("ChallengeDebug", "[AwardPoints] +$pointsEarned pts saved locally (guest)")
            } else {
                runCatching {
                    profileRepository.addArPoints(firebaseUid = user.uid, delta = pointsEarned)
                    Log.d("ChallengeDebug", "[AwardPoints] +$pointsEarned pts written to Strapi for uid=${user.uid}")
                }.onFailure { e ->
                    Log.e("ChallengeDebug", "[AwardPoints] failed to update profile", e)
                }
            }
        } finally {
            trace.stop()
        }
    }
}
