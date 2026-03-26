package com.khammin.authentication.data.repository

import com.khammin.authentication.data.remote.datasource.AuthRemoteDataSource
import com.khammin.authentication.domain.repository.AuthRepository
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

    override fun signOut() = remoteDataSource.signOut()
}