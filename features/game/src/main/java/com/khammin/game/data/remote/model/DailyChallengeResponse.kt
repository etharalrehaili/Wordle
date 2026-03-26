package com.khammin.game.data.remote.model

data class DailyChallengeResponse(val data: List<DailyChallengeItem>)

data class DailyChallengeItem(
    val id: Int,
    val documentId: String,
    val word: String,
    val language: String,
    val date: String,
)