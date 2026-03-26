package com.khammin.authentication.data.di

import com.khammin.authentication.data.remote.datasource.AuthRemoteDataSource
import com.khammin.authentication.data.remote.datasource.AuthRemoteDataSourceImpl
import com.khammin.authentication.data.repository.AuthRepositoryImpl
import com.khammin.authentication.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): AuthRemoteDataSource
}