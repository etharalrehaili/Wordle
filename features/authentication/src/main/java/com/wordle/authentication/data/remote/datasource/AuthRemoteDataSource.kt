package com.wordle.authentication.data.remote.datasource

interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun updateProfile(name: String): Result<Unit>
    suspend fun sendEmail(email: String): Result<Unit>
    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun reAuthenticate(password: String): Result<Unit>
    suspend fun changeEmail(newEmail: String): Result<Unit>
    fun signOut()
}