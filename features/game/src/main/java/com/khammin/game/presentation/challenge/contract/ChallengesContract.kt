package com.khammin.game.presentation.challenge.contract

import com.khammin.core.mvi.UiEffect
import com.khammin.core.mvi.UiIntent
import com.khammin.core.mvi.UiState
import com.khammin.game.domain.model.ChallengeDifficulty
import com.khammin.game.domain.model.ChallengeStatus

data class ChallengeUiItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val points: Int,
    val target: Int,
    val difficulty: ChallengeDifficulty,
    val iconName: String,
    val status: ChallengeStatus = ChallengeStatus.AVAILABLE,
    val progress: Int = 0,
)

data class ChallengesUiState(
    val isLoading: Boolean = true,
    /** Points earned from completed challenges (sum of their point values). */
    val totalPoints: Int = 0,
    /** All active challenges, ordered as returned from Firestore. */
    val challenges: List<ChallengeUiItem> = emptyList(),
) : UiState

sealed interface ChallengesIntent : UiIntent

sealed interface ChallengesEffect : UiEffect
