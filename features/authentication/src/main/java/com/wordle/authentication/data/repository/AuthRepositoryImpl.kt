package com.wordle.authentication.data.repository

import com.wordle.authentication.data.remote.datasource.AuthRemoteDataSource
import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        remoteDataSource.login(email, password)

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> =
        remoteDataSource.signUp(name, email, password)

    override suspend fun updateProfile(name: String): Result<Unit> =
        remoteDataSource.updateProfile(name)

    override fun signOut() = remoteDataSource.signOut()
}