package com.wordle.authentication.domain.usecase

import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class ChangeEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(newEmail: String): Result<Unit> =
        repository.changeEmail(newEmail)
}