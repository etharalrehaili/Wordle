package com.wordle.game.domain.model

data class Profile(
    val id: Int,
    val documentId: String,
    val firebaseUid: String,
    val name: String,
    val avatarUrl: String?,
    // English stats
    val enGamesPlayed: Int = 0,
    val enWordsSolved: Int = 0,
    val enWinPercentage: Double = 0.0,
    val enCurrentPoints: Int = 0,
    val enLastPlayedAt: String? = null,
    // Arabic stats
    val arGamesPlayed: Int = 0,
    val arWordsSolved: Int = 0,
    val arWinPercentage: Double = 0.0,
    val arCurrentPoints: Int = 0,
    val arLastPlayedAt: String? = null,
) {
    // Helper to get stats by language
    fun pointsForLanguage(language: String) =
        if (language == "ar") arCurrentPoints else enCurrentPoints

    fun gamesPlayedForLanguage(language: String) =
        if (language == "ar") arGamesPlayed else enGamesPlayed

    fun wordsSolvedForLanguage(language: String) =
        if (language == "ar") arWordsSolved else enWordsSolved

    fun winPercentageForLanguage(language: String) =
        if (language == "ar") arWinPercentage else enWinPercentage
}