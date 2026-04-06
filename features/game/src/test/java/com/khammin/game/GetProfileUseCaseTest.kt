package com.khammin.game

import android.content.Context
import android.net.Uri
import com.khammin.core.util.Resource
import com.khammin.game.domain.model.Profile
import com.khammin.game.domain.repository.ProfileRepository
import com.khammin.game.domain.usecases.profile.GetProfileUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetProfileUseCaseTest {

    private lateinit var repository: ProfileRepository
    private lateinit var useCase: GetProfileUseCase

    private val aProfile = Profile(
        id = 1,
        documentId = "doc123",
        firebaseUid = "uid123",
        name = "Alice",
        avatarUrl = "https://example.com/avatar.png",
        enGamesPlayed = 10,
        enWordsSolved = 7,
        enWinPercentage = 70.0,
        enCurrentPoints = 350,
    )

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetProfileUseCase(repository)
    }

    // -----------------------------------------------------------------------
    // Happy-path: repository returns a profile
    // -----------------------------------------------------------------------

    @Test
    fun `returns Success with profile when repository returns a profile`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val result = useCase("uid123")

        assertTrue(result is Resource.Success)
        assertEquals(aProfile, (result as Resource.Success).data)
    }

    @Test
    fun `calls repository with correct firebaseUid`() = runTest {
        whenever(repository.getProfile("uid-xyz")).thenReturn(aProfile)

        useCase("uid-xyz")

        verify(repository).getProfile("uid-xyz")
    }

    // -----------------------------------------------------------------------
    // Happy-path: repository returns null (profile not found)
    // -----------------------------------------------------------------------

    @Test
    fun `returns Success with null when repository returns null`() = runTest {
        whenever(repository.getProfile("unknown-uid")).thenReturn(null)

        val result = useCase("unknown-uid")

        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data)
    }

    // -----------------------------------------------------------------------
    // Error-path: repository throws
    // -----------------------------------------------------------------------

    @Test
    fun `returns Error with exception message when repository throws`() = runTest {
        val throwingRepo = stubThrowingRepo(RuntimeException("Network failure"))

        val result = GetProfileUseCase(throwingRepo)("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Network failure", (result as Resource.Error).message)
    }

    @Test
    fun `returns Error with fallback message when exception has no message`() = runTest {
        val throwingRepo = stubThrowingRepo(RuntimeException())

        val result = GetProfileUseCase(throwingRepo)("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Unknown error", (result as Resource.Error).message)
    }

    @Test
    fun `wraps any exception type as Error`() = runTest {
        val throwingRepo = stubThrowingRepo(IllegalStateException("Bad state"))

        val result = GetProfileUseCase(throwingRepo)("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Bad state", (result as Resource.Error).message)
    }

    // -----------------------------------------------------------------------
    // Profile helper methods
    // -----------------------------------------------------------------------

    @Test
    fun `profile pointsForLanguage returns enCurrentPoints for en`() {
        assertEquals(350, aProfile.pointsForLanguage("en"))
    }

    @Test
    fun `profile pointsForLanguage returns arCurrentPoints for ar`() {
        val arProfile = aProfile.copy(arCurrentPoints = 200)
        assertEquals(200, arProfile.pointsForLanguage("ar"))
    }

    @Test
    fun `profile gamesPlayedForLanguage returns correct value per language`() {
        assertEquals(10, aProfile.gamesPlayedForLanguage("en"))
        assertEquals(0, aProfile.gamesPlayedForLanguage("ar"))
    }

    @Test
    fun `profile wordsSolvedForLanguage returns correct value per language`() {
        assertEquals(7, aProfile.wordsSolvedForLanguage("en"))
        assertEquals(0, aProfile.wordsSolvedForLanguage("ar"))
    }

    @Test
    fun `profile winPercentageForLanguage returns correct value per language`() {
        assertEquals(70.0, aProfile.winPercentageForLanguage("en"), 0.001)
        assertEquals(0.0, aProfile.winPercentageForLanguage("ar"), 0.001)
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private fun stubThrowingRepo(ex: Exception) = object : ProfileRepository {
        override suspend fun getProfile(firebaseUid: String): Profile? = throw ex
        override suspend fun createProfile(firebaseUid: String, email: String): Profile =
            error("not used")
        override suspend fun updateProfile(
            documentId: String, firebaseUid: String, name: String, avatarUrl: String?,
            language: String, gamesPlayed: Int, wordsSolved: Int, winPercentage: Double,
            currentPoints: Int,
        ): Profile = error("not used")
        override suspend fun uploadAvatar(imageUri: Uri, context: Context): String =
            error("not used")
        override suspend fun getLeaderboard(limit: Int, language: String): List<Profile> =
            error("not used")
    }
}
