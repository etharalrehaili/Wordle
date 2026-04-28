package com.khammin.authentication.domain.repository

interface AuthRepository {
    suspend fun updateProfile(name: String): Result<Unit>
    fun signOut()
}