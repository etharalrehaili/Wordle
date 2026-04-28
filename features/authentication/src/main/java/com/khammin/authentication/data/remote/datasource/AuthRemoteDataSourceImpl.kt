package com.khammin.authentication.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRemoteDataSource {

    override suspend fun updateProfile(name: String): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No logged in user")
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            ).await()
        }

    override fun signOut() = auth.signOut()

}