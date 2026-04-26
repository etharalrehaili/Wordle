package com.khammin.game.domain.model

/**
 * A challenge definition fetched from Firestore's `challengeDefinitions` collection.
 *
 * Firestore document shape:
 * ```
 * challengeDefinitions/{id}
 *   id:              "win_3_tries"
 *   titleAr:         "اربح في 3 محاولات"
 *   titleEn:         "Win in 3 tries"
 *   points:          400
 *   target:          1          // 1 = one-shot, >1 = incremental progress bar
 *   difficulty:      "INTERMEDIATE"   // BEGINNER | INTERMEDIATE | EXPERT
 *   conditionType:   "WIN_UNDER_GUESSES"
 *   conditionParams: { maxGuesses: 3, gameMode: "SOLO" }
 *   iconName:        "bolt"     // see ChallengesScreen.iconForName()
 *   isActive:        true       // false = hidden from UI and evaluation
 * ```
 */
data class RemoteChallengeDefinition(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val points: Int,
    /** 1 = one-shot; >1 = incremental (progress bar shown). */
    val target: Int,
    val difficulty: ChallengeDifficulty,
    val conditionType: ChallengeConditionType,
    /** Flexible key-value pairs parsed from the Firestore map field. */
    val conditionParams: Map<String, Any> = emptyMap(),
    val iconName: String = "star",
    val isActive: Boolean = true,
)
