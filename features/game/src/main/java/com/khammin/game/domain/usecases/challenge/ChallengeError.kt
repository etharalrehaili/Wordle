package com.khammin.game.domain.usecases.challenge

sealed class ChallengeError {
    object NoChallenge : ChallengeError() {
        const val KEY = "challenge_error_no_challenge"
    }
}
