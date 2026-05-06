package com.khammin.game.data.remote.model

// ── Upload ────────────────────────────────────────────────────────────────────

data class StrapiUploadResponse(
    val id: Int,
    val url: String
)

// ── Single response wrapper ───────────────────────────────────────────────────

data class SingleProfileResponse(val data: ProfileItem)

// ── Create profile ────────────────────────────────────────────────────────────

data class CreateProfileRequest(val data: CreateProfileData)

data class CreateProfileData(
    val firebaseUid: String,
    val name: String,
)

// ── Update profile ────────────────────────────────────────────────────────────

data class UpdateProfileRequest(val data: UpdateProfileData)

data class UpdateProfileData(
    val name: String,
    val avatarUrl: String?,
    val arGamesPlayed: Int,
    val arWordsSolved: Int,
    val arWinPercentage: Double,
    val arCurrentPoints: Int,
    val arLastPlayedAt: String?,
)