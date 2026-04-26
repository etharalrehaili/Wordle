package com.khammin.game.domain.model

data class UserChallenge(
    val id: String,
    val status: ChallengeStatus = ChallengeStatus.AVAILABLE,
    val progress: Int = 0,
)
