package com.wordle.game.data.remote.model

data class ProfileResponse(
    val data: List<ProfileItem>
)

data class ProfileItem(
    val id: Int,
    val documentId: String,
    val firebaseUid: String,
    val name: String,
    val avatarUrl: String?,
    // English stats
    val enGamesPlayed: Int = 0,
    val enWordsSolved: Int = 0,
    val enWinPercentage: Double = 0.0,
    val enCurrentPoints: Int = 0,
    val enLastPlayedAt: String? = null,
    // Arabic stats
    val arGamesPlayed: Int = 0,
    val arWordsSolved: Int = 0,
    val arWinPercentage: Double = 0.0,
    val arCurrentPoints: Int = 0,
    val arLastPlayedAt: String? = null,
)