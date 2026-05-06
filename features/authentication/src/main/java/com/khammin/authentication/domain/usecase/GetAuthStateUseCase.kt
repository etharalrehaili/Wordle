package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.getAuthState()
}