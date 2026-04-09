package com.khammin.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameProgress(
    val easyWordsSolved: Int    = 0,  // 4-letter wins
    val classicWordsSolved: Int = 0,  // 5-letter wins
)
