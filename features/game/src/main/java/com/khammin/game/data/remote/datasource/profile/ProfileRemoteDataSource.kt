package com.khammin.game.data.remote.datasource.profile

import android.net.Uri
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.domain.model.ProfileUpdate

interface ProfileRemoteDataSource {
    suspend fun getProfile(firebaseUid: String): ProfileItem?

    suspend fun createProfile(firebaseUid: String, name: String): ProfileItem

    suspend fun updateProfile(update: ProfileUpdate): ProfileItem

    suspend fun uploadAvatar(imageUri: Uri): String

    suspend fun syncProfile(data: ProfileSyncData): ProfileItem

    suspend fun getLeaderboard(limit: Int, language: String): List<ProfileItem>
}