package com.wordle.game.data.repository

import android.content.Context
import android.net.Uri
import com.wordle.game.data.remote.datasource.profile.ProfileRemoteDataSource
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remote: ProfileRemoteDataSource
) : ProfileRepository {

    override suspend fun getProfile(firebaseUid: String): ProfileItem? {
        return remote.getProfile(firebaseUid)
    }

    override suspend fun createProfile(firebaseUid: String, email: String): ProfileItem {
        return remote.createProfile(firebaseUid, email)
    }

    override suspend fun updateProfile(
        documentId: String,
        name: String,
        avatarUrl: String?,
        gamesPlayed: Int,
        wordsSolved: Int,
        winPercentage: Double,
        currentPoints: Int,
    ): ProfileItem {
        return remote.updateProfile(documentId, name, avatarUrl, gamesPlayed, wordsSolved, winPercentage, currentPoints)
    }

    override suspend fun uploadAvatar(imageUri: Uri, context: Context): String {
        return remote.uploadAvatar(imageUri, context)
    }

    override suspend fun getLeaderboard(limit: Int): List<ProfileItem> {
        return remote.getLeaderboard(limit)
    }
}