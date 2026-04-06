package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SendEmailUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var sendEmailUseCase: SendEmailUseCase

    @Before
    fun setUp() {
        repository = mock()
        sendEmailUseCase = SendEmailUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository sendEmail and returns success`() = runTest {
        val email = "user@example.com"
        whenever(repository.sendEmail(email)).thenReturn(Result.success(Unit))

        val result = sendEmailUseCase(email)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke delegates to repository sendEmail and returns failure`() = runTest {
        val email = "notfound@example.com"
        val exception = RuntimeException("User not found")
        whenever(repository.sendEmail(email)).thenReturn(Result.failure(exception))

        val result = sendEmailUseCase(email)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke passes correct email to repository`() = runTest {
        val email = "reset@domain.com"
        whenever(repository.sendEmail(email)).thenReturn(Result.success(Unit))

        sendEmailUseCase(email)

        verify(repository).sendEmail(email)
    }
}
