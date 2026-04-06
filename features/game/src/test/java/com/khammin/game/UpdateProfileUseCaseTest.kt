package com.khammin.game

import android.content.Context
import android.net.Uri
import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.profile.UpdateProfileUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdateProfileUseCaseTest {

    private lateinit var repository: ProfileRepository
    private lateinit var useCase: UpdateProfileUseCase

    private val aProfile = Profile(
        id = 1,
        documentId = "doc123",
        firebaseUid = "uid123",
        name = "Alice",
        avatarUrl = "https://example.com/avatar.png",
        enGamesPlayed = 5,
        enWordsSolved = 3,
        enWinPercentage = 60.0,
        enCurrentPoints = 150,
    )

    @Before
    fun setUp() {
        repository = mock()
        useCase = UpdateProfileUseCase(repository)
    }

    @Test
    fun `returns Success with updated profile on success`() = runTest {
        whenever(repository.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(aProfile)

        val result = useCase(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = "https://example.com/avatar.png",
            language = "en",
            gamesPlayed = 5,
            wordsSolved = 3,
            winPercentage = 60.0,
            currentPoints = 150,
        )

        assertTrue(result is Resource.Success)
        assertEquals(aProfile, (result as Resource.Success).data)
    }

    @Test
    fun `returns Error when repository throws`() = runTest {
        val throwingRepo = object : ProfileRepository {
            override suspend fun getProfile(firebaseUid: String): Profile? = error("not used")
            override suspend fun createProfile(firebaseUid: String, email: String): Profile = error("not used")
            override suspend fun updateProfile(
                documentId: String, firebaseUid: String, name: String, avatarUrl: String?,
                language: String, gamesPlayed: Int, wordsSolved: Int, winPercentage: Double,
                currentPoints: Int,
            ): Profile = throw RuntimeException("Network error")
            override suspend fun uploadAvatar(imageUri: Uri, context: Context): String = error("not used")
            override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> = error("not used")
        }
        val result = UpdateProfileUseCase(throwingRepo)(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "en",
        )

        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `returns Error with fallback message when exception has no message`() = runTest {
        val throwingRepo = object : ProfileRepository {
            override suspend fun getProfile(firebaseUid: String): Profile? = error("not used")
            override suspend fun createProfile(firebaseUid: String, email: String): Profile = error("not used")
            override suspend fun updateProfile(
                documentId: String, firebaseUid: String, name: String, avatarUrl: String?,
                language: String, gamesPlayed: Int, wordsSolved: Int, winPercentage: Double,
                currentPoints: Int,
            ): Profile = throw RuntimeException()
            override suspend fun uploadAvatar(imageUri: Uri, context: Context): String = error("not used")
            override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> = error("not used")
        }
        val result = UpdateProfileUseCase(throwingRepo)(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "ar",
        )

        assertTrue(result is Resource.Error)
        assertEquals("Unknown error", (result as Resource.Error).message)
    }

    @Test
    fun `uses default values for optional stats parameters`() = runTest {
        whenever(repository.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(aProfile)

        useCase(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "en",
        )

        verify(repository).updateProfile(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "en",
            gamesPlayed = 0,
            wordsSolved = 0,
            winPercentage = 0.0,
            currentPoints = 0,
        )
    }

    @Test
    fun `passes null avatarUrl to repository`() = runTest {
        whenever(repository.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(aProfile.copy(avatarUrl = null))

        val result = useCase(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "en",
        )

        assertTrue(result is Resource.Success)
        verify(repository).updateProfile(
            documentId = "doc123",
            firebaseUid = "uid123",
            name = "Alice",
            avatarUrl = null,
            language = "en",
            gamesPlayed = 0,
            wordsSolved = 0,
            winPercentage = 0.0,
            currentPoints = 0,
        )
    }
}
