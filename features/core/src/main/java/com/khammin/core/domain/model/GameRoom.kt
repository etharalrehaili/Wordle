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
    @JvmField var isCustomWord: Boolean = false

)

data class PlayerState(
    val guesses: List<String> = emptyList(),  // ["S,T,O,R,M", "C,L,O,U,D"]
    val types: List<String> = emptyList(),    // ["CORRECT,ABSENT,PRESENT", ...]
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val solved: Boolean = false,
    val finishedAt: Long? = null
)