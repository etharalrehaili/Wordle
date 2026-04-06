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

class SignUpUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var signUpUseCase: SignUpUseCase

    @Before
    fun setUp() {
        repository = mock()
        signUpUseCase = SignUpUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository signUp and returns success`() = runTest {
        val email = "newuser@example.com"
        val password = "newpassword"
        whenever(repository.signUp(email, password)).thenReturn(Result.success(Unit))

        val result = signUpUseCase(email, password)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke delegates to repository signUp and returns failure`() = runTest {
        val email = "existing@example.com"
        val password = "password"
        val exception = RuntimeException("Email already in use")
        whenever(repository.signUp(email, password)).thenReturn(Result.failure(exception))

        val result = signUpUseCase(email, password)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke passes correct email and password to repository`() = runTest {
        val email = "signup@domain.com"
        val password = "strongPass1"
        whenever(repository.signUp(email, password)).thenReturn(Result.success(Unit))

        signUpUseCase(email, password)

        verify(repository).signUp(email, password)
    }
}
