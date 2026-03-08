package com.wordle.authentication.domain.usecase

import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String): Result<Unit> =
        repository.updateProfile(name)
}