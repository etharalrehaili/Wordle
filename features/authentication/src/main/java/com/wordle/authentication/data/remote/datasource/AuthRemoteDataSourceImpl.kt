package com.wordle.authentication.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Login failed")
            if (!user.isEmailVerified) {
                auth.signOut()
                error("Please verify your email before logging in.")
            }
            Unit
        }

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder().build()
            )?.await()
            auth.signOut()
        }

    override suspend fun updateProfile(name: String): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No logged in user")
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            ).await()
        }

    override suspend fun sendEmail(email: String): Result<Unit> =
        runCatching {
            auth.sendPasswordResetEmail(email).await()
        }

    override suspend fun sendVerificationEmail(): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No logged in user")
            user.sendEmailVerification().await()
        }

    override suspend fun reAuthenticate(password: String): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No logged in user")
            val email = user.email ?: error("No email found")
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, password)
            user.reauthenticate(credential).await()
        }

    override suspend fun changeEmail(newEmail: String): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No logged in user")
            android.util.Log.d("ChangeEmail", "Current user: ${user.email}")
            android.util.Log.d("ChangeEmail", "New email: $newEmail")
            android.util.Log.d("ChangeEmail", "Email verified: ${user.isEmailVerified}")
            user.verifyBeforeUpdateEmail(newEmail).await()
            android.util.Log.d("ChangeEmail", "Success - verification sent")
            Unit
        }.also { result ->
            result.onFailure { e ->
                android.util.Log.e("ChangeEmail", "Exception type: ${e::class.qualifiedName}")
                android.util.Log.e("ChangeEmail", "Exception message: ${e.message}")
                android.util.Log.e("ChangeEmail", "Full error:", e)
            }
        }

    override fun signOut() = auth.signOut()

}