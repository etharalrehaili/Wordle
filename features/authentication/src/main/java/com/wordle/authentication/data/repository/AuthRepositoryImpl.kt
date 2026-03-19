package com.wordle.authentication.data.repository

import com.wordle.authentication.data.remote.datasource.AuthRemoteDataSource
import com.wordle.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        remoteDataSource.login(email, password)

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        remoteDataSource.signUp(email, password)

    override suspend fun updateProfile(name: String): Result<Unit> =
        remoteDataSource.updateProfile(name)

    override suspend fun sendEmail(email: String): Result<Unit> =
        remoteDataSource.sendEmail(email)

    override suspend fun sendVerificationEmail(): Result<Unit> =
        remoteDataSource.sendVerificationEmail()

    override suspend fun reAuthenticate(password: String): Result<Unit> =
        remoteDataSource.reAuthenticate(password)

    override suspend fun changeEmail(newEmail: String): Result<Unit> =
        remoteDataSource.changeEmail(newEmail)

    override fun signOut() = remoteDataSource.signOut()
}