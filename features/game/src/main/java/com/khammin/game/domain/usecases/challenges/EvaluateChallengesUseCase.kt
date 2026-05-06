package com.khammin.game.domain.usecases.challenges

import com.google.firebase.auth.FirebaseAuth
import com.khammin.game.data.local.CompletedChallengesStore
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

class EvaluateChallengesUseCase @Inject constructor(
    private val progressRepository: ChallengeProgressRepository,
    private val definitionRepository: ChallengeDefinitionRepository,
    private val completedChallengesStore: CompletedChallengesStore,
    private val auth: FirebaseAuth,
) {
    suspend operator fun invoke(result: GameResult): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val definitions = definitionRepository.getDefinitions().filter { it.isActive }
        if (definitions.isEmpty()) return emptyList()

        val snapshot = progressRepository.getSnapshot(uid)

        val firestoreCompleted = snapshot.challenges
            .filter { it.value.status == ChallengeStatus.COMPLETED }
            .keys
        if (firestoreCompleted.isNotEmpty()) {
            completedChallengesStore.markCompleted(firestoreCompleted)
        }

        val locallyCompleted = completedChallengesStore.getAll()
        val updated          = snapshot.challenges.toMutableMap()
        val completed        = mutableListOf<String>()

        definitions.forEach { def ->
            if (def.id !in updated) updated[def.id] = UserChallenge(def.id)
        }

        for (def in definitions) {
            val current = updated[def.id] ?: UserChallenge(def.id)
            if (current.status == ChallengeStatus.COMPLETED || def.id in locallyCompleted) continue

            when (def.conditionType) {

                ChallengeConditionType.WIN_N_GAMES_STREAK -> {
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
                    // Handled separately below
                }

                ChallengeConditionType.PLAY_N_GAMES -> {
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

        val today     = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val newLastPlayedDate = today

        definitions.filter { it.conditionType == ChallengeConditionType.PLAY_N_CONSECUTIVE_DAYS }
            .forEach { def ->
                val current = updated[def.id] ?: UserChallenge(def.id)
                if (current.status == ChallengeStatus.COMPLETED || def.id in locallyCompleted) return@forEach

                val newDays = when (snapshot.lastPlayedDate) {
                    today     -> current.progress
                    yesterday -> current.progress + 1
                    else      -> 1
                }
                val newStatus = when {
                    newDays >= def.target -> ChallengeStatus.COMPLETED
                    newDays > 1           -> ChallengeStatus.IN_PROGRESS
                    else                  -> ChallengeStatus.AVAILABLE
                }
                if (newStatus == ChallengeStatus.COMPLETED) completed += def.id
                updated[def.id] = current.copy(status = newStatus, progress = newDays)
            }

        if (completed.isNotEmpty()) {
            completedChallengesStore.markCompleted(completed)
        }

        progressRepository.saveSnapshot(
            uid,
            ChallengeSnapshot(challenges = updated, lastPlayedDate = newLastPlayedDate)
        )
        return completed
    }

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

            ChallengeConditionType.PLAY_N_GAMES,
            ChallengeConditionType.WIN_N_MULTIPLAYER,
            ChallengeConditionType.WIN_N_GAMES_STREAK,
            ChallengeConditionType.PLAY_N_CONSECUTIVE_DAYS -> false
        }
    }
}