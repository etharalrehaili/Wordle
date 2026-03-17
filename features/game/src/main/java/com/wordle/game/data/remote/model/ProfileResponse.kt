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
    val gamesPlayed: Int,
    val wordsSolved: Int,
    val winPercentage: Double,
    val currentPoints: Int,
    val lastPlayedAt: String? = null,
)