package com.khammin.game.domain.model

/**
 * Returns the points awarded for solving a multiplayer word in [guessCount] attempts.
 * Fewer guesses = more points. Returns 0 for a non-win (guessCount <= 0).
 */
fun pointsForGuessCount(guessCount: Int): Int = when (guessCount) {
    1    -> 100
    2    -> 80
    3    -> 60
    4    -> 40
    5    -> 20
    else -> 10
}
