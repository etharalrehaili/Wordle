package com.khammin.game.presentation.game.vm

import android.net.Uri
import com.khammin.core.util.Resource
import com.khammin.game.domain.usecases.profile.GetGuestProfileUseCase
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import com.khammin.game.domain.usecases.profile.SaveGuestProfileUseCase
import com.khammin.game.domain.usecases.profile.UploadAvatarUseCase
import javax.inject.Inject

data class MyProfileData(
    val name: String,
    val avatarUrl: String?,
    val avatarColor: Long?,
    val avatarEmoji: String?,
    /** Hosted URL to push to Firestore so other players can load the avatar. */
    val hostedAvatarUrl: String?,
)

class MultiplayerProfileLoader @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getGuestProfileUseCase: GetGuestProfileUseCase,
    private val saveGuestProfileUseCase: SaveGuestProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
) {
    /** Load profile for a logged-in (Google) user from Strapi, falling back to Firebase photo. */
    suspend fun loadLoggedInProfile(
        myId: String,
        firebasePhotoUrl: String?,
        fallbackName: String,
    ): MyProfileData? {
        val result = getProfileUseCase(myId, forceRefresh = true)
        if (result !is Resource.Success) return null
        val name = result.data?.name?.takeIf { it.isNotBlank() } ?: fallbackName
        val photoUrl = result.data?.avatarUrl ?: firebasePhotoUrl
        return MyProfileData(name, photoUrl, null, null, photoUrl)
    }

    /**
     * Load profile for an anonymous (guest) user from DataStore.
     * Uploads the local avatar to Strapi if present so other devices can load it.
     * Persists a generated name on first launch.
     */
    suspend fun loadGuestProfile(fallbackName: String): MyProfileData {
        val saved = getGuestProfileUseCase()
        if (saved == null) {
            saveGuestProfileUseCase(fallbackName, null, null)
            return MyProfileData(fallbackName, null, null, null, null)
        }
        val name = saved.name?.takeIf { it.isNotBlank() } ?: fallbackName
        val hostedUrl: String? = if (saved.avatarUri != null) {
            val result = runCatching { uploadAvatarUseCase(Uri.parse(saved.avatarUri)) }.getOrNull()
            (result as? Resource.Success)?.data
        } else null
        return MyProfileData(
            name            = name,
            avatarUrl       = saved.avatarUri,
            avatarColor     = saved.avatarColor,
            avatarEmoji     = saved.avatarEmoji,
            hostedAvatarUrl = hostedUrl,
        )
    }

    /** Persist an updated guest profile locally. */
    suspend fun saveGuestProfile(name: String, avatarColor: Long?, avatarEmoji: String?) {
        runCatching { saveGuestProfileUseCase(name, avatarColor, avatarEmoji) }
    }

    /** Fetch a Strapi profile for any user (opponent name, guest info). Returns (name, avatarUrl). */
    suspend fun fetchProfile(userId: String, forceRefresh: Boolean = true): Pair<String?, String?> {
        val result = getProfileUseCase(userId, forceRefresh)
        return if (result is Resource.Success) {
            result.data?.name?.takeIf { it.isNotBlank() } to result.data?.avatarUrl
        } else null to null
    }
}
