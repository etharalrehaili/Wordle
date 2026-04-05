package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        repository = mock()
        loginUseCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository login and returns success`() = runTest {
        val email = "user@example.com"
        val password = "secret"
        whenever(repository.login(email, password)).thenReturn(Result.success(Unit))

        val result = loginUseCase(email, password)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke delegates to repository login and returns failure`() = runTest {
        val email = "user@example.com"
        val password = "wrong"
        val exception = RuntimeException("Invalid credentials")
        whenever(repository.login(email, password)).thenReturn(Result.failure(exception))

        val result = loginUseCase(email, password)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke passes correct email and password to repository`() = runTest {
        val email = "test@domain.com"
        val password = "p@ssw0rd"
        whenever(repository.login(email, password)).thenReturn(Result.success(Unit))

        loginUseCase(email, password)

        org.mockito.kotlin.verify(repository).login(email, password)
    }
}
