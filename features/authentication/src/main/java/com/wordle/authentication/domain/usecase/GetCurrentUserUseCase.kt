package com.wordle.authentication.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    operator fun invoke(): FirebaseUser? = auth.currentUser
}