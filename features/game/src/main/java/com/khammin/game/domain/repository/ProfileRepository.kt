package com.khammin.game.domain.repository

import android.net.Uri
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.model.ProfileUpdate
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(firebaseUid: String, forceRefresh: Boolean = false): Profile?

    fun observeProfile(firebaseUid: String): Flow<Profile?>

    suspend fun createProfile(firebaseUid: String, email: String): Profile

    suspend fun updateProfile(update: ProfileUpdate): Profile

    suspend fun uploadAvatar(imageUri: Uri): String

    suspend fun getLeaderboard(limit: Int, language: String): List<Profile>

    suspend fun addArPoints(firebaseUid: String, delta: Int): Profile

    suspend fun syncPendingUpdates()
}