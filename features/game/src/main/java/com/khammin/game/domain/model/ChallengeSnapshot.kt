package com.khammin.game.domain.model

data class ChallengeSnapshot(
    val challenges: Map<String, UserChallenge> = emptyMap(),
    val lastPlayedDate: String = "",
)
