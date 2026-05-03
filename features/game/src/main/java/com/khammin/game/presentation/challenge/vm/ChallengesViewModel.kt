package com.khammin.game.presentation.challenge.vm

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.mvi.BaseMviViewModel
import com.khammin.core.util.NetworkUtils
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.game.domain.usecases.challenges.GetChallengeDefinitionsUseCase
import com.khammin.game.domain.usecases.challenges.GetChallengeProgressUseCase
import com.khammin.game.presentation.challenge.contract.ChallengeUiItem
import com.khammin.game.presentation.challenge.contract.ChallengesEffect
import com.khammin.game.presentation.challenge.contract.ChallengesIntent
import com.khammin.game.presentation.challenge.contract.ChallengesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val getChallengeProgressUseCase: GetChallengeProgressUseCase,
    private val getChallengeDefinitionsUseCase: GetChallengeDefinitionsUseCase,
    private val networkUtils: NetworkUtils,
) : BaseMviViewModel<ChallengesIntent, ChallengesUiState, ChallengesEffect>(
    initialState = ChallengesUiState()
) {
    private var collectJob: Job? = null
    private var currentUid: String? = null
    private var wasAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser ?: return@AuthStateListener
        val uid = user.uid
        when {
            // New UID seen — covers two cases:
            //   1. First launch: currentUid was null while ensureAnonymousAuth() was running;
            //      anonymous sign-in completed after init{} already ran, so startCollecting
            //      was never called.
            //   2. Full sign-out then sign-in with a different account.
            uid != currentUid -> {
                currentUid = uid
                wasAnonymous = user.isAnonymous
                startCollecting(uid)
            }
            // Same UID but transitioned anonymous → Google (account linking).
            wasAnonymous && !user.isAnonymous -> {
                wasAnonymous = false
                startCollecting(uid)
            }
        }
    }

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        currentUid = uid
        if (uid != null) {
            startCollecting(uid)
        } else {
            setState { copy(isLoading = false) }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onEvent(intent: ChallengesIntent) {
        when (intent) {
            ChallengesIntent.Refresh -> refresh()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun refresh() {
        val uid = currentUid ?: return
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            if (!networkUtils.isConnected()) {
                setState { copy(isRefreshing = false, noInternet = true) }
                return@launch
            }
            // If the initial load was skipped because there was no internet, collectJob will
            // be inactive. Start collection now that we have connectivity.
            if (collectJob?.isActive != true) {
                setState { copy(noInternet = false, error = null) }
                startCollecting(uid)
                // isRefreshing = false will be set by the flow's first emission.
                return@launch
            }
            // Flow is live and self-updating — just give visual feedback.
            delay(400)
            setState { copy(isRefreshing = false, noInternet = false, error = null) }
        }
    }

    // ACCESS_NETWORK_STATE is a normal (install-time) permission declared in the manifest —
    // @SuppressLint is safe here and avoids propagating @RequiresPermission into init/lambdas.
    @SuppressLint("MissingPermission")
    private fun startCollecting(uid: String) {
        if (!networkUtils.isConnected()) {
            setState { copy(isLoading = false, noInternet = true) }
            return
        }
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            try {
                combine(
                    getChallengeDefinitionsUseCase(),
                    getChallengeProgressUseCase(uid),
                ) { definitions, snapshot ->
                    definitions to snapshot
                }.collect { (definitions, snapshot) ->
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

                    setState {
                        copy(
                            isLoading    = false,
                            isRefreshing = false,
                            noInternet   = false,
                            challenges   = uiItems,
                            totalPoints  = totalPoints,
                        )
                    }
                }
            } catch (e: Exception) {
                val isNoInternet = e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("No address associated") == true
                setState {
                    copy(
                        isLoading    = false,
                        isRefreshing = false,
                        noInternet   = isNoInternet,
                        error        = if (isNoInternet) null else e.message,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}
