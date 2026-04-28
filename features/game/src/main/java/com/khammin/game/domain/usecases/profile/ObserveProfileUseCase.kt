package com.khammin.game.domain.usecases.profile

import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a [Flow] that emits the locally-cached [Profile] for [firebaseUid]
 * and re-emits every time the Room row changes (e.g. after a game win updates stats).
 *
 * Backed by the Room DAO — no network call is made. The cache is kept up-to-date
 * by [GetProfileUseCase] (on first launch) and [ProfileRepositoryImpl.updateProfile]
 * (after each game win).
 */
class ObserveProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    operator fun invoke(firebaseUid: String): Flow<Profile?> =
        repo.observeProfile(firebaseUid)
}
