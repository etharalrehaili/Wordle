package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ObserveHeartbeatAfkUseCaseTest {

    private lateinit var repo: MultiplayerRepository
    private lateinit var useCase: ObserveHeartbeatAfkUseCase

    @Before
    fun setUp() {
        repo = mock()
        useCase = ObserveHeartbeatAfkUseCase(repo)
    }

    @Test
    fun `invoke delegates to repository observeHeartbeatAfk with correct arguments`() = runTest {
        val roomId = "room123"
        val userId = "user456"
        whenever(repo.observeHeartbeatAfk(roomId, userId)).thenReturn(flowOf(false))

        useCase(roomId, userId)

        verify(repo).observeHeartbeatAfk(roomId, userId)
    }

    @Test
    fun `invoke returns flow that emits false when player is active`() = runTest {
        val roomId = "room123"
        val userId = "user456"
        whenever(repo.observeHeartbeatAfk(roomId, userId)).thenReturn(flowOf(false))

        val results = useCase(roomId, userId).toList()

        assertEquals(listOf(false), results)
    }

    @Test
    fun `invoke returns flow that emits true when player is AFK`() = runTest {
        val roomId = "room123"
        val userId = "user456"
        whenever(repo.observeHeartbeatAfk(roomId, userId)).thenReturn(flowOf(true))

        val results = useCase(roomId, userId).toList()

        assertEquals(listOf(true), results)
    }

    @Test
    fun `invoke returns flow that emits multiple AFK state transitions`() = runTest {
        val roomId = "room-abc"
        val userId = "user-xyz"
        whenever(repo.observeHeartbeatAfk(roomId, userId)).thenReturn(flowOf(false, true, false))

        val results = useCase(roomId, userId).toList()

        assertEquals(listOf(false, true, false), results)
    }
}
