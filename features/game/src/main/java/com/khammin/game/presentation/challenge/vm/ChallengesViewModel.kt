package com.khammin.game.presentation.challenge.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.usecases.challenges.GetChallengeDefinitionsUseCase
import com.khammin.game.domain.usecases.challenges.GetChallengeProgressUseCase
import com.khammin.game.presentation.challenge.contract.ChallengeUiItem
import com.khammin.game.presentation.challenge.contract.ChallengesEffect
import com.khammin.game.presentation.challenge.contract.ChallengesIntent
import com.khammin.game.presentation.challenge.contract.ChallengesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val getChallengeProgressUseCase: GetChallengeProgressUseCase,
    private val getChallengeDefinitionsUseCase: GetChallengeDefinitionsUseCase,
) : BaseMviViewModel<ChallengesIntent, ChallengesUiState, ChallengesEffect>(
    initialState = ChallengesUiState()
) {
    private var collectJob: Job? = null
    private var wasAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser ?: return@AuthStateListener
        if (wasAnonymous && !user.isAnonymous) {
            wasAnonymous = false
            startCollecting(user.uid)
        }
    }

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            startCollecting(uid)
        } else {
            setState { copy(isLoading = false) }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun startCollecting(uid: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            combine(
                getChallengeDefinitionsUseCase(),
                getChallengeProgressUseCase(uid),
            ) { definitions, snapshot ->
                definitions to snapshot
            }.collect { (definitions, snapshot) ->
                Log.d("ChallengeDebug", "[ChallengesVM] definitions=${definitions.size} snapshot=${snapshot.challenges.size}")

                val uiItems = definitions
                    .filter { it.isActive }
                    .map { def ->
                        val userChallenge = snapshot.challenges[def.id]
                        ChallengeUiItem(
                            id         = def.id,
                            titleAr    = def.titleAr,
                            titleEn    = def.titleEn,
                            points     = def.points,
                            target     = def.target,
                            difficulty = def.difficulty,
                            iconName   = def.iconName,
                            status     = userChallenge?.status ?: ChallengeStatus.AVAILABLE,
                            progress   = userChallenge?.progress ?: 0,
                        )
                    }

                val totalPoints = uiItems
                    .filter { it.status == ChallengeStatus.COMPLETED }
                    .sumOf { it.points }

                Log.d("ChallengeDebug", "[ChallengesVM] totalPoints=$totalPoints")
                setState {
                    copy(
                        isLoading   = false,
                        challenges  = uiItems,
                        totalPoints = totalPoints,
                    )
                }
            }
        }
    }

    override fun onEvent(intent: ChallengesIntent) = Unit

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}
