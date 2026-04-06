package com.khammin.authentication.domain.usecase

import com.khammin.authentication.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SignOutUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setUp() {
        repository = mock()
        signOutUseCase = SignOutUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository signOut`() {
        signOutUseCase()

        verify(repository).signOut()
    }

    @Test
    fun `invoke calls repository signOut exactly once`() {
        signOutUseCase()
        signOutUseCase()

        org.mockito.kotlin.verify(repository, org.mockito.kotlin.times(2)).signOut()
    }
}
