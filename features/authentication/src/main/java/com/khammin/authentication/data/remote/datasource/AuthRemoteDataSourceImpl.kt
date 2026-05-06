package com.khammin.authentication.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRemoteDataSource {

    override fun signOut() = auth.signOut()

}