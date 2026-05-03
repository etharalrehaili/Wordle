package com.khammin.game.presentation.game.vm

import com.khammin.game.domain.usecases.game.ObserveOpponentPresenceUseCase
import com.khammin.game.domain.usecases.game.RegisterPresenceUseCase
import com.khammin.game.domain.usecases.game.UpdatePresenceStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultiplayerPresenceManager @Inject constructor(
    private val registerPresenceUseCase: RegisterPresenceUseCase,
    private val updatePresenceStateUseCase: UpdatePresenceStateUseCase,
    private val observeOpponentPresenceUseCase: ObserveOpponentPresenceUseCase,
) {
    private val presenceDropJobs = mutableMapOf<String, Job>()

    suspend fun register(roomId: String, userId: String) {
        runCatching { registerPresenceUseCase(roomId, userId) }
    }

    suspend fun updateForeground(roomId: String, userId: String, isForeground: Boolean) {
        runCatching { updatePresenceStateUseCase(roomId, userId, isForeground) }
    }

    fun observe(
        roomId: String,
        userId: String,
        scope: CoroutineScope,
        onDropped: suspend (userId: String) -> Unit,
    ) {
        observeOpponentPresenceUseCase(roomId, userId).onEach { isOnline ->
            if (isOnline) {
                presenceDropJobs[userId]?.cancel()
                presenceDropJobs.remove(userId)
            } else {
                if (presenceDropJobs[userId]?.isActive != true) {
                    presenceDropJobs[userId] = scope.launch {
                        delay(30_000L)
                        onDropped(userId)
                    }
                }
            }
        }.launchIn(scope)
    }
}
