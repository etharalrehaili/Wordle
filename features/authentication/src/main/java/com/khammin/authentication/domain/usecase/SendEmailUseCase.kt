package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> =
        repository.sendEmail(email)
}