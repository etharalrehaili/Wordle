package com.khammin.game.domain.model

enum class GameMode { SOLO, DAILY_CHALLENGE, MULTIPLAYER }

/**
 * Snapshot of a completed game passed to [EvaluateChallengesUseCase].
 * Decouples challenge evaluation logic from any specific ViewModel.
 *
 * @param isWin           true if the player solved the word
 * @param guessCount      1-indexed number of guesses used (1 = first try)
 * @param timeTakenSeconds elapsed seconds from game start to final guess
 * @param wordLength      length of the target word (4, 5, or 6)
 * @param gameMode        which game mode produced this result
 * @param hintsUsed       number of hints used (only meaningful in SOLO mode)
 * @param language        "en" or "ar"
 */
data class GameResult(
    val isWin: Boolean,
    val guessCount: Int,
    val timeTakenSeconds: Long,
    val wordLength: Int,
    val gameMode: GameMode,
    val hintsUsed: Int = 0,
    val language: String,
)
