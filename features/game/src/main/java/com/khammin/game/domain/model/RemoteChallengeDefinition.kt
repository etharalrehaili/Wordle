package com.khammin.game.domain.model

data class RemoteChallengeDefinition(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val points: Int,
    val target: Int,
    val difficulty: ChallengeDifficulty,
    val conditionType: ChallengeConditionType,
    val conditionParams: Map<String, Any> = emptyMap(),
    val iconName: String = "star",
    val isActive: Boolean = true,
)
