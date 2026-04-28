package com.khammin.authentication.data.remote.datasource

interface AuthRemoteDataSource {
    suspend fun updateProfile(name: String): Result<Unit>
    fun signOut()
}