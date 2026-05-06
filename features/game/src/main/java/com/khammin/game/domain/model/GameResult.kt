package com.khammin.game.domain.model

enum class GameMode { SOLO, DAILY_CHALLENGE, MULTIPLAYER }

data class GameResult(
    val isWin: Boolean,
    val guessCount: Int,
    val timeTakenSeconds: Long,
    val wordLength: Int,
    val gameMode: GameMode,
    val hintsUsed: Int = 0,
    val language: String,
)
