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
    val isRefreshing: Boolean = false,
    val noInternet: Boolean = false,
    val error: String? = null,
    val totalPoints: Int = 0,
    val challenges: List<ChallengeUiItem> = emptyList(),
) : UiState

sealed interface ChallengesIntent : UiIntent {
    data object Refresh : ChallengesIntent
}

sealed interface ChallengesEffect : UiEffect
