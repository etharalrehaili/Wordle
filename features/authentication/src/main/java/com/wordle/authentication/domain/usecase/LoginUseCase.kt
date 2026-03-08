package com.wordle.authentication.domain.usecase

import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password)
}