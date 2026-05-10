package com.khammin.game.domain.usecases.game

import com.khammin.game.domain.repository.MultiplayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SendHeartbeatUseCaseTest {

    private lateinit var repo: MultiplayerRepository
    private lateinit var useCase: SendHeartbeatUseCase

    @Before
    fun setUp() {
        repo = mock()
        useCase = SendHeartbeatUseCase(repo)
    }

    @Test
    fun `invoke delegates to repository sendHeartbeat with correct arguments`() = runTest {
        val roomId = "room123"
        val userId = "user456"

        useCase(roomId, userId)

        verify(repo).sendHeartbeat(roomId, userId)
    }

    @Test
    fun `invoke uses the exact roomId and userId provided`() = runTest {
        val roomId = "another-room"
        val userId = "another-user"

        useCase(roomId, userId)

        verify(repo).sendHeartbeat("another-room", "another-user")
    }
}
