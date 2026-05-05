package com.khammin.game.domain.usecases.stats

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.util.Resource
import com.khammin.game.data.local.GuestProfileDataStore
import com.khammin.game.data.local.LocalStatsDataStore
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Migrates locally-saved guest stats (game counts, challenge points, display name, avatar)
 * to the Strapi profile after the user signs in with Google. Called from
 * [HomeViewModel.ensureProfileExists] so the profile is guaranteed to exist.
 *
 * Stats are additive (remote + local). Local data is cleared after a successful push.
 */
class MigrateLocalStatsUseCase @Inject constructor(
    private val localStats: LocalStatsDataStore,
    private val guestProfileDataStore: GuestProfileDataStore,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (user.isAnonymous) return

        val enPlayed    = localStats.getGamesPlayed("en")
        val enSolved    = localStats.getWordsSolved("en")
        val arPlayed    = localStats.getGamesPlayed("ar")
        val arSolved    = localStats.getWordsSolved("ar")
        val localPoints = localStats.getTotalPoints()
        val guestProfile = guestProfileDataStore.getProfile()

        val hasStats        = enPlayed > 0 || arPlayed > 0 || localPoints > 0
        val hasGuestProfile = guestProfile != null

        if (!hasStats && !hasGuestProfile) return

        val profile = when (val result = getProfileUseCase(user.uid)) {
            is Resource.Success -> result.data ?: return
            else -> return
        }

        if (enPlayed > 0) {
            val newPlayed = profile.enGamesPlayed + enPlayed
            val newSolved = profile.enWordsSolved + enSolved
            updateProfileUseCase(
                documentId    = profile.documentId,
                firebaseUid   = user.uid,
                name          = profile.name,
                avatarUrl     = profile.avatarUrl,
                language      = "en",
                gamesPlayed   = newPlayed,
                wordsSolved   = newSolved,
                winPercentage = if (newPlayed > 0) newSolved.toDouble() / newPlayed * 100.0 else 0.0,
                currentPoints = profile.enCurrentPoints,
            )
        }

        if (arPlayed > 0) {
            val newPlayed = profile.arGamesPlayed + arPlayed
            val newSolved = profile.arWordsSolved + arSolved
            updateProfileUseCase(
                documentId    = profile.documentId,
                firebaseUid   = user.uid,
                name          = profile.name,
                avatarUrl     = profile.avatarUrl,
                language      = "ar",
                gamesPlayed   = newPlayed,
                wordsSolved   = newSolved,
                winPercentage = if (newPlayed > 0) newSolved.toDouble() / newPlayed * 100.0 else 0.0,
                currentPoints = profile.arCurrentPoints,
            )
        }

        if (localPoints > 0) {
            runCatching {
                profileRepository.addArPoints(firebaseUid = user.uid, delta = localPoints)
            }
        }

        // Migrate guest display name and avatar URI to the Strapi profile
        if (hasGuestProfile) {
            val guestName = guestProfile!!.name.takeIf { it.isNotBlank() }
            val guestAvatarUri = guestProfile.avatarUri?.takeIf { it.isNotBlank() }

            val migratedName = guestName ?: profile.name

            val migratedAvatarUrl = if (guestAvatarUri != null) {
                runCatching {
                    when (val upload = uploadAvatarUseCase(Uri.parse(guestAvatarUri), context)) {
                        is Resource.Success -> upload.data
                        else -> profile.avatarUrl
                    }
                }.getOrDefault(profile.avatarUrl)
            } else {
                profile.avatarUrl
            }

            if (migratedName != profile.name || migratedAvatarUrl != profile.avatarUrl) {
                updateProfileUseCase(
                    documentId    = profile.documentId,
                    firebaseUid   = user.uid,
                    name          = migratedName,
                    avatarUrl     = migratedAvatarUrl,
                    language      = "en",
                    gamesPlayed   = profile.enGamesPlayed + enPlayed,
                    wordsSolved   = profile.enWordsSolved + enSolved,
                    winPercentage = run {
                        val p = profile.enGamesPlayed + enPlayed
                        val s = profile.enWordsSolved + enSolved
                        if (p > 0) s.toDouble() / p * 100.0 else 0.0
                    },
                    currentPoints = profile.enCurrentPoints,
                )
            }

            guestProfileDataStore.clearProfile()
        }

        localStats.clearAll()
    }
}
