package com.khammin.game.domain.model

/**
 * Groups all parameters needed to update a remote profile.
 * Stats fields default to 0 for name/avatar-only updates (e.g. from ProfileViewModel).
 */
data class ProfileUpdate(
    val documentId: String,
    val firebaseUid: String,
    val name: String,
    val avatarUrl: String?,
    val language: String,
    val gamesPlayed: Int = 0,
    val wordsSolved: Int = 0,
    val winPercentage: Double = 0.0,
    val currentPoints: Int = 0,
)
