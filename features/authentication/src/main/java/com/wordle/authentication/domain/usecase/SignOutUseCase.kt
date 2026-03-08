package com.wordle.authentication.domain.usecase

import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.signOut()
}