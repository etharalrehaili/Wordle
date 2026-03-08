package com.wordle.authentication.domain.usecase

import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<Unit> =
        repository.signUp(name, email, password)
}
