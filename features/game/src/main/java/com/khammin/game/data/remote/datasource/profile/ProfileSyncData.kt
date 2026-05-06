package com.khammin.game.data.remote.datasource.profile

/** Groups the ar-stats fields needed to sync a pending offline profile update. */
data class ProfileSyncData(
    val documentId: String,
    val firebaseUid: String,
    val name: String,
    val avatarUrl: String?,
    val arGamesPlayed: Int,
    val arWordsSolved: Int,
    val arWinPercentage: Double,
    val arCurrentPoints: Int,
    val arLastPlayedAt: String?,
)
