package com.khammin.game.domain.model

/**
 * Static metadata for every achievement challenge.
 * Progress/status is stored in Firestore; only points, target, and difficulty live here.
 */
object ChallengeDefinitions {

    // ── Stable IDs ────────────────────────────────────────────────────────────
    const val GUESS_4           = "guess_4"
    const val GUESS_6           = "guess_6"
    const val FIRST_GUESS       = "first_guess"
    const val PLAY_3_GAMES      = "play_3_games"
    const val GUESS_2MIN        = "guess_2min"
    const val GUESS_STREAK      = "guess_streak"
    const val WIN_MULTIPLAYER   = "win_multiplayer"
    const val WORD_5_LETTERS    = "5_letter_word"
    const val GUESS_1MIN        = "guess_1min"
    const val GUESS_EXPERT      = "guess_expert"
    const val GUESS_2_TRIES     = "guess_2_tries"
    const val WIN_5_MULTIPLAYER = "win_5_multiplayer"
    const val PERFECT_WEEK      = "perfect_week"
    const val GUESS_30S         = "30s"

    data class Definition(
        val id: String,
        val points: Int,
        /** 1 = one-shot; >1 = incremental (progress bar shown) */
        val target: Int,
        val difficulty: ChallengeDifficulty,
    )

    val all: List<Definition> = listOf(
        Definition(GUESS_4,            250, 1, ChallengeDifficulty.BEGINNER),
        Definition(GUESS_6,             50, 1, ChallengeDifficulty.BEGINNER),
        Definition(FIRST_GUESS,        100, 1, ChallengeDifficulty.BEGINNER),
        Definition(PLAY_3_GAMES,        75, 3, ChallengeDifficulty.BEGINNER),
        Definition(GUESS_2MIN,         200, 1, ChallengeDifficulty.INTERMEDIATE),
        Definition(GUESS_STREAK,       300, 3, ChallengeDifficulty.INTERMEDIATE),
        Definition(WIN_MULTIPLAYER,    350, 1, ChallengeDifficulty.INTERMEDIATE),
        Definition(WORD_5_LETTERS,     275, 1, ChallengeDifficulty.INTERMEDIATE),
        Definition(GUESS_1MIN,         400, 1, ChallengeDifficulty.INTERMEDIATE),
        Definition(GUESS_EXPERT,       500, 1, ChallengeDifficulty.EXPERT),
        Definition(GUESS_2_TRIES,      600, 1, ChallengeDifficulty.EXPERT),
        Definition(WIN_5_MULTIPLAYER,  750, 5, ChallengeDifficulty.EXPERT),
        Definition(PERFECT_WEEK,      1000, 7, ChallengeDifficulty.EXPERT),
        Definition(GUESS_30S,          800, 1, ChallengeDifficulty.EXPERT),
    )

    val byId: Map<String, Definition> = all.associateBy { it.id }
}
