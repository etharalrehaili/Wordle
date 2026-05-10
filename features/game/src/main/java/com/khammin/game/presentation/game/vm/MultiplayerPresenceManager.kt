package com.khammin.game.presentation.game.vm

import com.khammin.game.domain.usecases.game.CleanupPresenceUseCase
import com.khammin.game.domain.usecases.game.ObserveHeartbeatAfkUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentAfkUseCase
import com.khammin.game.domain.usecases.game.ObserveOpponentPresenceUseCase
import com.khammin.game.domain.usecases.game.RegisterPresenceUseCase
import com.khammin.game.domain.usecases.game.SendHeartbeatUseCase
import com.khammin.game.domain.usecases.game.UpdatePresenceStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PRESENCE_DROP_TIMEOUT_MS = 120_000L
private const val HEARTBEAT_INTERVAL_MS    =  15_000L

class MultiplayerPresenceManager @Inject constructor(
    private val registerPresenceUseCase: RegisterPresenceUseCase,
    private val updatePresenceStateUseCase: UpdatePresenceStateUseCase,
    private val observeOpponentPresenceUseCase: ObserveOpponentPresenceUseCase,
    private val observeOpponentAfkUseCase: ObserveOpponentAfkUseCase,
    private val observeHeartbeatAfkUseCase: ObserveHeartbeatAfkUseCase,
    private val sendHeartbeatUseCase: SendHeartbeatUseCase,
    private val cleanupPresenceUseCase: CleanupPresenceUseCase,
) {
    private val presenceDropJobs = mutableMapOf<String, Job>()
    private val heartbeatJobs    = mutableMapOf<String, Job>()

    suspend fun register(roomId: String, userId: String) {
        runCatching { registerPresenceUseCase(roomId, userId) }
    }

    fun cleanup(roomId: String, userId: String) {
        stopHeartbeat(roomId, userId)
        runCatching { cleanupPresenceUseCase(roomId, userId) }
    }

    suspend fun updateForeground(roomId: String, userId: String, isForeground: Boolean) {
        runCatching { updatePresenceStateUseCase(roomId, userId, isForeground) }
    }

    /** Start writing heartbeat timestamps every 15 s for [userId] in [roomId]. */
    fun startHeartbeat(roomId: String, userId: String, scope: CoroutineScope) {
        val key = "$roomId/$userId"
        if (heartbeatJobs[key]?.isActive == true) return
        heartbeatJobs[key] = scope.launch {
            while (true) {
                runCatching { sendHeartbeatUseCase(roomId, userId) }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    fun stopHeartbeat(roomId: String, userId: String) {
        val key = "$roomId/$userId"
        heartbeatJobs.remove(key)?.cancel()
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

    /** Observe heartbeat-based AFK for [userId] (host-side: watches opponent's heartbeat node). */
    fun observeHeartbeatAfk(
        roomId: String,
        userId: String,
        scope: CoroutineScope,
        onAfkChanged: (userId: String, isAfk: Boolean) -> Unit,
    ) {
        observeHeartbeatAfkUseCase(roomId, userId).onEach { isAfk ->
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
                        delay(PRESENCE_DROP_TIMEOUT_MS)
                        onDropped(userId)
                    }
                }
            }
        }.launchIn(scope)
    }
}
