package com.khammin.game.domain.usecases.stats

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.util.Resource
import com.khammin.game.data.local.GuestProfileDataStore
import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.model.ProfileUpdate
import com.khammin.game.domain.model.winPercentage
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import javax.inject.Inject

class MigrateLocalStatsUseCase @Inject constructor(
    private val localStats: LocalStatsDataStore,
    private val guestProfileDataStore: GuestProfileDataStore,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth,
) {
    suspend operator fun invoke() {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return

        val arPlayed     = localStats.getGamesPlayed("ar")
        val arSolved     = localStats.getWordsSolved("ar")
        val localPoints  = localStats.getTotalPoints()
        val guestProfile = guestProfileDataStore.getProfile()

        val hasStats        = arPlayed > 0 || localPoints > 0
        val hasGuestProfile = guestProfile != null

        if (!hasStats && !hasGuestProfile) return

        val profile = when (val result = getProfileUseCase(user.uid)) {
            is Resource.Success -> result.data ?: return
            else -> return
        }

        if (arPlayed > 0) {
            val newPlayed = profile.arGamesPlayed + arPlayed
            val newSolved = profile.arWordsSolved + arSolved
            updateProfileUseCase(
                ProfileUpdate(
                    documentId    = profile.documentId,
                    firebaseUid   = user.uid,
                    name          = profile.name,
                    avatarUrl     = profile.avatarUrl,
                    language      = "ar",
                    gamesPlayed   = newPlayed,
                    wordsSolved   = newSolved,
                    winPercentage = winPercentage(newPlayed, newSolved),
                    currentPoints = profile.arCurrentPoints,
                )
            )
        }

        if (localPoints > 0) {
            runCatching {
                profileRepository.addArPoints(firebaseUid = user.uid, delta = localPoints)
            }
        }

        if (hasGuestProfile) {
            val guestName      = guestProfile!!.name.takeIf { it.isNotBlank() }
            val guestAvatarUri = guestProfile.avatarUri?.takeIf { it.isNotBlank() }
            val migratedName   = guestName ?: profile.name

            val migratedAvatarUrl = if (guestAvatarUri != null) {
                runCatching {
                    when (val upload = uploadAvatarUseCase(Uri.parse(guestAvatarUri))) {
                        is Resource.Success -> upload.data
                        else -> profile.avatarUrl
                    }
                }.getOrDefault(profile.avatarUrl)
            } else {
                profile.avatarUrl
            }

            if (migratedName != profile.name || migratedAvatarUrl != profile.avatarUrl) {
                val newPlayed = profile.arGamesPlayed + arPlayed
                val newSolved = profile.arWordsSolved + arSolved
                updateProfileUseCase(
                    ProfileUpdate(
                        documentId    = profile.documentId,
                        firebaseUid   = user.uid,
                        name          = migratedName,
                        avatarUrl     = migratedAvatarUrl,
                        language      = "ar",
                        gamesPlayed   = newPlayed,
                        wordsSolved   = newSolved,
                        winPercentage = winPercentage(newPlayed, newSolved),
                        currentPoints = profile.arCurrentPoints,
                    )
                )
            }

            guestProfileDataStore.clearProfile()
        }

        localStats.clearAll()
    }
}