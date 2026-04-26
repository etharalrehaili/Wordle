package com.khammin.game.domain.usecases.challenges

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.khammin.game.domain.model.ChallengeConditionType
import com.khammin.game.domain.model.ChallengeSnapshot
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.model.GameMode
import com.khammin.game.domain.model.GameResult
import com.khammin.game.domain.model.RemoteChallengeDefinition
import com.khammin.game.domain.model.UserChallenge
import com.khammin.game.domain.repository.ChallengeDefinitionRepository
import com.khammin.game.domain.repository.ChallengeProgressRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Evaluates a completed game against every active challenge definition fetched from Firestore,
 * updates the user's progress document, and returns the IDs of any challenges newly completed.
 *
 * Call once per game-over event from GameViewModel / ChallengeViewModel / MultiplayerGameViewModel.
 */
class EvaluateChallengesUseCase @Inject constructor(
    private val progressRepository: ChallengeProgressRepository,
    private val definitionRepository: ChallengeDefinitionRepository,
) {
    /** @return list of challenge IDs that transitioned to COMPLETED in this evaluation. */
    suspend operator fun invoke(result: GameResult): List<String> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ChallengeDebug", "[Evaluate] uid=$uid result=$result")
        if (uid == null) {
            Log.w("ChallengeDebug", "[Evaluate] uid is null — user not signed in, aborting")
            return emptyList()
        }

        val definitions = definitionRepository.getDefinitions().filter { it.isActive }
        Log.d("ChallengeDebug", "[Evaluate] loaded ${definitions.size} active definitions")
        if (definitions.isEmpty()) return emptyList()

        val snapshot  = progressRepository.getSnapshot(uid)
        val updated   = snapshot.challenges.toMutableMap()
        val completed = mutableListOf<String>()

        // Ensure every known definition has a progress entry
        definitions.forEach { def ->
            if (def.id !in updated) updated[def.id] = UserChallenge(def.id)
        }

        // ── Evaluate each definition ──────────────────────────────────────────

        for (def in definitions) {
            val current = updated[def.id] ?: UserChallenge(def.id)
            if (current.status == ChallengeStatus.COMPLETED) continue

            when (def.conditionType) {

                ChallengeConditionType.WIN_N_GAMES_STREAK -> {
                    // Special: resets to 0 on any loss
                    val newProgress = if (result.isWin) current.progress + 1 else 0
                    val newStatus   = when {
                        newProgress >= def.target -> ChallengeStatus.COMPLETED
                        newProgress > 0           -> ChallengeStatus.IN_PROGRESS
                        else                      -> ChallengeStatus.AVAILABLE
                    }
                    if (newStatus == ChallengeStatus.COMPLETED) completed += def.id
                    updated[def.id] = current.copy(status = newStatus, progress = newProgress)
                }

                ChallengeConditionType.PLAY_N_CONSECUTIVE_DAYS -> {
                    // Handled separately below (needs lastPlayedDate)
                }

                ChallengeConditionType.PLAY_N_GAMES -> {
                    // Always increment — any game outcome counts
                    val newProgress = current.progress + 1
                    val newStatus   = if (newProgress >= def.target) ChallengeStatus.COMPLETED
                                      else ChallengeStatus.IN_PROGRESS
                    if (newStatus == ChallengeStatus.COMPLETED) completed += def.id
                    updated[def.id] = current.copy(status = newStatus, progress = newProgress)
                }

                ChallengeConditionType.WIN_N_MULTIPLAYER -> {
                    if (result.isWin && result.gameMode == GameMode.MULTIPLAYER) {
                        val newProgress = current.progress + 1
                        val newStatus   = if (newProgress >= def.target) ChallengeStatus.COMPLETED
                                          else ChallengeStatus.IN_PROGRESS
                        if (newStatus == ChallengeStatus.COMPLETED) completed += def.id
                        updated[def.id] = current.copy(status = newStatus, progress = newProgress)
                    }
                }

                else -> {
                    // One-shot challenges
                    if (meetsCondition(def, result)) {
                        completed += def.id
                        updated[def.id] = current.copy(
                            status   = ChallengeStatus.COMPLETED,
                            progress = def.target,
                        )
                    }
                }
            }
        }

        // ── PLAY_N_CONSECUTIVE_DAYS — needs lastPlayedDate ────────────────────
        val today     = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        val newLastPlayedDate: String = today
        definitions.filter { it.conditionType == ChallengeConditionType.PLAY_N_CONSECUTIVE_DAYS }
            .forEach { def ->
                val current = updated[def.id] ?: UserChallenge(def.id)
                if (current.status == ChallengeStatus.COMPLETED) return@forEach

                val newDays = when (snapshot.lastPlayedDate) {
                    today     -> current.progress      // already counted today
                    yesterday -> current.progress + 1  // extending streak
                    else      -> 1                     // streak broken or first day
                }
                val newStatus = when {
                    newDays >= def.target -> ChallengeStatus.COMPLETED
                    newDays > 1           -> ChallengeStatus.IN_PROGRESS
                    else                  -> ChallengeStatus.AVAILABLE
                }
                if (newStatus == ChallengeStatus.COMPLETED) completed += def.id
                updated[def.id] = current.copy(status = newStatus, progress = newDays)
            }

        Log.d("ChallengeDebug", "[Evaluate] Saving snapshot — newly completed=$completed")
        progressRepository.saveSnapshot(
            uid,
            ChallengeSnapshot(challenges = updated, lastPlayedDate = newLastPlayedDate)
        )
        Log.d("ChallengeDebug", "[Evaluate] Snapshot saved successfully")
        return completed
    }

    // ── One-shot condition evaluator ──────────────────────────────────────────

    private fun meetsCondition(def: RemoteChallengeDefinition, result: GameResult): Boolean {
        val p = def.conditionParams
        return when (def.conditionType) {
            ChallengeConditionType.WIN_ANY ->
                result.isWin

            ChallengeConditionType.WIN_UNDER_GUESSES -> {
                val max  = (p["maxGuesses"] as? Long)?.toInt() ?: return false
                val mode = p["gameMode"] as? String
                result.isWin &&
                    result.guessCount < max &&
                    (mode == null || result.gameMode.name == mode)
            }

            ChallengeConditionType.WIN_EXACT_GUESSES -> {
                val exact = (p["guesses"] as? Long)?.toInt() ?: return false
                result.isWin && result.guessCount == exact
            }

            ChallengeConditionType.WIN_UNDER_SECONDS -> {
                val secs = (p["seconds"] as? Long) ?: return false
                result.isWin && result.timeTakenSeconds <= secs
            }

            ChallengeConditionType.WIN_NO_HINTS -> {
                val mode = p["gameMode"] as? String
                result.isWin &&
                    result.hintsUsed == 0 &&
                    (mode == null || result.gameMode.name == mode)
            }

            ChallengeConditionType.WIN_WORD_LENGTH -> {
                val len = (p["wordLength"] as? Long)?.toInt() ?: return false
                result.isWin && result.wordLength == len
            }

            ChallengeConditionType.WIN_MULTIPLAYER ->
                result.isWin && result.gameMode == GameMode.MULTIPLAYER

            // Incremental types are handled above — should never reach here
            ChallengeConditionType.PLAY_N_GAMES,
            ChallengeConditionType.WIN_N_MULTIPLAYER,
            ChallengeConditionType.WIN_N_GAMES_STREAK,
            ChallengeConditionType.PLAY_N_CONSECUTIVE_DAYS -> false
        }
    }
}
