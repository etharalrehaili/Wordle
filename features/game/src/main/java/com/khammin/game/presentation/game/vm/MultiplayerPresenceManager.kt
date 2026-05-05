package com.khammin.game.presentation.game.vm

import com.khammin.game.domain.usecases.game.CleanupPresenceUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentAfkUseCase
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
    private val observeOpponentAfkUseCase: ObserveOpponentAfkUseCase,
    private val cleanupPresenceUseCase: CleanupPresenceUseCase,
) {
    private val presenceDropJobs = mutableMapOf<String, Job>()

    suspend fun register(roomId: String, userId: String) {
        runCatching { registerPresenceUseCase(roomId, userId) }
    }

    fun cleanup(roomId: String, userId: String) {
        runCatching { cleanupPresenceUseCase(roomId, userId) }
    }

    suspend fun updateForeground(roomId: String, userId: String, isForeground: Boolean) {
        runCatching { updatePresenceStateUseCase(roomId, userId, isForeground) }
    }

    fun observeAfk(
        roomId: String,
        userId: String,
        scope: CoroutineScope,
        onAfkChanged: (userId: String, isAfk: Boolean) -> Unit,
    ) {
        observeOpponentAfkUseCase(roomId, userId).onEach { isAfk ->
            onAfkChanged(userId, isAfk)
        }.launchIn(scope)
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
                    // 2-minute window: gives Firebase time to reconnect from background
                    // and restore presence via the .info/connected listener.
                    // If still offline after 2 min the app is truly closed → remove.
                    presenceDropJobs[userId] = scope.launch {
                        delay(120_000L)
                        onDropped(userId)
                    }
                }
            }
        }.launchIn(scope)
    }
}
