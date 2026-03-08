package com.wordle.authentication.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun updateProfile(name: String): Result<Unit>
    fun signOut()
}