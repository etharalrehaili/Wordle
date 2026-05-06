package com.khammin.authentication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.khammin.authentication.data.remote.datasource.AuthRemoteDataSource
import com.khammin.authentication.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val auth: FirebaseAuth,
) : AuthRepository {

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun signOut() = remoteDataSource.signOut()
}