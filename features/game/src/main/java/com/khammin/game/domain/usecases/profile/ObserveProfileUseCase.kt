package com.khammin.game.domain.usecases.profile

import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    operator fun invoke(firebaseUid: String): Flow<Profile?> =
        repo.observeProfile(firebaseUid)
}
