package com.khammin.authentication.data.repository

import com.khammin.authentication.data.remote.datasource.AuthRemoteDataSource
import com.khammin.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun updateProfile(name: String): Result<Unit> =
        remoteDataSource.updateProfile(name)

    override fun signOut() = remoteDataSource.signOut()
}