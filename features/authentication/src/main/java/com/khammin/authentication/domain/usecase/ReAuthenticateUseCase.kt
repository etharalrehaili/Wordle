package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class ReAuthenticateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> =
        repository.reAuthenticate(password)
}