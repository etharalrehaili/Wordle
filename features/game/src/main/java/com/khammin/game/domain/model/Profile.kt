package com.khammin.game.domain.model

/** Returns the win percentage for [solved] wins out of [played] games, or 0 if unplayed. */
fun winPercentage(played: Int, solved: Int): Double =
    if (played > 0) solved.toDouble() / played * 100.0 else 0.0

data class Profile(
    val id: Int,
    val documentId: String,
    val firebaseUid: String,
    val name: String,
    val avatarUrl: String?,
    val arGamesPlayed: Int = 0,
    val arWordsSolved: Int = 0,
    val arWinPercentage: Double = 0.0,
    val arCurrentPoints: Int = 0,
    val arLastPlayedAt: String? = null,
)