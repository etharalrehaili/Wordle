package com.wordle.game.domain.repository

import android.content.Context
import android.net.Uri
import com.wordle.game.data.remote.model.ProfileItem

interface ProfileRepository {
    suspend fun getProfile(firebaseUid: String): ProfileItem?
    suspend fun createProfile(firebaseUid: String, email: String): ProfileItem
    suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem
    suspend fun uploadAvatar(imageUri: Uri, context: Context): String
    suspend fun getLeaderboard(limit: Int): List<ProfileItem>
}