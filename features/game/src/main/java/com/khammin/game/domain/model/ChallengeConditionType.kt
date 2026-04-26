package com.khammin.game.domain.model

enum class ChallengeConditionType {
    /** Win any game (any mode, any word length). */
    WIN_ANY,

    /** Win with strictly fewer than conditionParams["maxGuesses"] guesses.
     *  Optional conditionParams["gameMode"] restricts to SOLO / MULTIPLAYER / DAILY_CHALLENGE. */
    WIN_UNDER_GUESSES,

    /** Win in exactly conditionParams["guesses"] guesses. */
    WIN_EXACT_GUESSES,

    /** Win within conditionParams["seconds"] elapsed seconds. */
    WIN_UNDER_SECONDS,

    /** Win without using any hints.
     *  Optional conditionParams["gameMode"] to restrict game mode. */
    WIN_NO_HINTS,

    /** Win a game where the word length == conditionParams["wordLength"]. */
    WIN_WORD_LENGTH,

    /** Win a multiplayer game (one-shot). */
    WIN_MULTIPLAYER,

    /** Play N games regardless of outcome (incremental, target set via Definition.target). */
    PLAY_N_GAMES,

    /** Win N multiplayer games (incremental, target set via Definition.target). */
    WIN_N_MULTIPLAYER,

    /** Win N consecutive games – resets to 0 on any loss (target set via Definition.target). */
    WIN_N_GAMES_STREAK,

    /** Play at least once on N consecutive calendar days (target set via Definition.target). */
    PLAY_N_CONSECUTIVE_DAYS,
}
