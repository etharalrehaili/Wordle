package com.khammin.game.domain.usecases.profile

import com.khammin.game.data.local.GuestProfileData
import com.khammin.game.data.local.GuestProfileDataStore
import javax.inject.Inject

class GetGuestProfileUseCase @Inject constructor(
    private val store: GuestProfileDataStore
) {
    suspend operator fun invoke(): GuestProfileData? = store.getProfile()
}
