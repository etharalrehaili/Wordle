package com.khammin.core.domain.model

data class GameRoom(
    val roomId: String = "",
    val hostId: String = "",
    val guestId: String = "",
    val guestIds: List<String> = emptyList(), // multi-player custom word guests
    val word: String = "",
    val language: String = "",
    val wordLength: Int = 0,
    val status: String = "waiting", // waiting | playing | finished
    val winnerId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val leftBy: String = "",
    val failedBy: String = "",
    @JvmField var isCustomWord: Boolean = false,
    val playAgainVotes: List<String> = emptyList(), // guestIds who voted to play again
    val roundNumber: Int = 1,
    val totalPoints: Map<String, Int> = emptyMap(), // guestId -> cumulative points (legacy)
    val sessionPoints: Map<String, Int> = emptyMap(), // guestId -> session-total points (updated after each round)
    val guestProfiles: Map<String, Map<String, String>> = emptyMap(), // guestId -> {name, avatarColor, avatarEmoji}
)

data class PlayerState(
    val guesses: List<String> = emptyList(),  // ["S,T,O,R,M", "C,L,O,U,D"]
    val types: List<String> = emptyList(),    // ["CORRECT,ABSENT,PRESENT", ...]
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val solved: Boolean = false,
    val finishedAt: Long? = null
)