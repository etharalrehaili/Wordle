package com.khammin.authentication.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getAuthState(): Flow<Boolean>
    fun signOut()
}