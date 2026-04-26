package com.khammin.game.domain.usecases.profile

import com.khammin.game.data.local.GuestProfileDataStore
import javax.inject.Inject

class SaveGuestProfileUseCase @Inject constructor(
    private val store: GuestProfileDataStore
) {
    suspend operator fun invoke(
        name: String,
        avatarColor: Long?,
        avatarEmoji: String?,
        avatarUri: String? = null,
    ) = store.saveProfile(name, avatarColor, avatarEmoji, avatarUri)
}
