package com.khammin.game.domain.model

/**
 * Full state of a user's challenge progress stored in Firestore.
 *
 * @param challenges      map of challenge ID → [UserChallenge] progress
 * @param lastPlayedDate  ISO date string of the last day a game was completed,
 *                        used to compute the [ChallengeDefinitions.PERFECT_WEEK] streak
 */
data class ChallengeSnapshot(
    val challenges: Map<String, UserChallenge> = emptyMap(),
    val lastPlayedDate: String = "",
)
