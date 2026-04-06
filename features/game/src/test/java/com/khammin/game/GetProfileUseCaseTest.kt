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
        enLastPlayedAt = "2026-04-06",
        arGamesPlayed = 3,
        arWordsSolved = 2,
        arWinPercentage = 66.6,
        arCurrentPoints = 90,
        arLastPlayedAt = null,
    )

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetProfileUseCase(repository)
    }

    // -------------------------------------------------------------------------
    // Happy-path: profile found
    // -------------------------------------------------------------------------

    @Test
    fun `returns Success with profile when repository returns a profile`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val result = useCase("uid123")

        assertTrue(result is Resource.Success)
        assertEquals(aProfile, (result as Resource.Success).data)
    }

    @Test
    fun `delegates the firebaseUid to the repository`() = runTest {
        whenever(repository.getProfile("uid-abc")).thenReturn(aProfile)

        useCase("uid-abc")

        verify(repository).getProfile("uid-abc")
    }

    // -------------------------------------------------------------------------
    // Happy-path: no profile found (null)
    // -------------------------------------------------------------------------

    @Test
    fun `returns Success with null when repository returns null`() = runTest {
        whenever(repository.getProfile("uid-missing")).thenReturn(null)

        val result = useCase("uid-missing")

        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data)
    }

    // -------------------------------------------------------------------------
    // Error paths
    // -------------------------------------------------------------------------

    @Test
    fun `returns Error when repository throws an exception with a message`() = runTest {
        val throwingRepo = object : ProfileRepository {
            override suspend fun getProfile(firebaseUid: String): Profile? =
                throw RuntimeException("Network error")
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

        val result = GetProfileUseCase(throwingRepo)("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `returns Error with fallback message when exception has no message`() = runTest {
        val throwingRepo = object : ProfileRepository {
            override suspend fun getProfile(firebaseUid: String): Profile? =
                throw RuntimeException()
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

        val result = GetProfileUseCase(throwingRepo)("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Unknown error", (result as Resource.Error).message)
    }

    @Test
    fun `returns Error when repository throws an IllegalStateException`() = runTest {
        whenever(repository.getProfile("uid123"))
            .thenThrow(IllegalStateException("Illegal state"))

        val result = useCase("uid123")

        assertTrue(result is Resource.Error)
        assertEquals("Illegal state", (result as Resource.Error).message)
    }

    // -------------------------------------------------------------------------
    // Profile content correctness
    // -------------------------------------------------------------------------

    @Test
    fun `returned profile contains correct English stats`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val result = useCase("uid123") as Resource.Success

        val profile = result.data!!
        assertEquals(10, profile.enGamesPlayed)
        assertEquals(7, profile.enWordsSolved)
        assertEquals(70.0, profile.enWinPercentage, 0.001)
        assertEquals(350, profile.enCurrentPoints)
    }

    @Test
    fun `returned profile contains correct Arabic stats`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val result = useCase("uid123") as Resource.Success

        val profile = result.data!!
        assertEquals(3, profile.arGamesPlayed)
        assertEquals(2, profile.arWordsSolved)
        assertEquals(66.6, profile.arWinPercentage, 0.001)
        assertEquals(90, profile.arCurrentPoints)
    }

    @Test
    fun `pointsForLanguage returns English points for 'en'`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val profile = (useCase("uid123") as Resource.Success).data!!

        assertEquals(350, profile.pointsForLanguage("en"))
    }

    @Test
    fun `pointsForLanguage returns Arabic points for 'ar'`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val profile = (useCase("uid123") as Resource.Success).data!!

        assertEquals(90, profile.pointsForLanguage("ar"))
    }

    @Test
    fun `gamesPlayedForLanguage returns correct values`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val profile = (useCase("uid123") as Resource.Success).data!!

        assertEquals(10, profile.gamesPlayedForLanguage("en"))
        assertEquals(3, profile.gamesPlayedForLanguage("ar"))
    }

    @Test
    fun `wordsSolvedForLanguage returns correct values`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val profile = (useCase("uid123") as Resource.Success).data!!

        assertEquals(7, profile.wordsSolvedForLanguage("en"))
        assertEquals(2, profile.wordsSolvedForLanguage("ar"))
    }

    @Test
    fun `winPercentageForLanguage returns correct values`() = runTest {
        whenever(repository.getProfile("uid123")).thenReturn(aProfile)

        val profile = (useCase("uid123") as Resource.Success).data!!

        assertEquals(70.0, profile.winPercentageForLanguage("en"), 0.001)
        assertEquals(66.6, profile.winPercentageForLanguage("ar"), 0.001)
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    fun `profile with null avatarUrl is returned correctly`() = runTest {
        val profileNoAvatar = aProfile.copy(avatarUrl = null)
        whenever(repository.getProfile("uid123")).thenReturn(profileNoAvatar)

        val result = useCase("uid123") as Resource.Success

        assertNull(result.data!!.avatarUrl)
    }

    @Test
    fun `profile with default zero stats is returned correctly`() = runTest {
        val freshProfile = Profile(
            id = 2,
            documentId = "doc456",
            firebaseUid = "uid456",
            name = "Bob",
            avatarUrl = null,
        )
        whenever(repository.getProfile("uid456")).thenReturn(freshProfile)

        val result = useCase("uid456") as Resource.Success

        val profile = result.data!!
        assertEquals(0, profile.enGamesPlayed)
        assertEquals(0, profile.enWordsSolved)
        assertEquals(0.0, profile.enWinPercentage, 0.001)
        assertEquals(0, profile.enCurrentPoints)
        assertEquals(0, profile.arGamesPlayed)
        assertEquals(0, profile.arCurrentPoints)
    }

    @Test
    fun `each call delegates to the repository independently`() = runTest {
        val profileA = aProfile.copy(firebaseUid = "uid-a", name = "Alice")
        val profileB = aProfile.copy(firebaseUid = "uid-b", name = "Bob")
        whenever(repository.getProfile("uid-a")).thenReturn(profileA)
        whenever(repository.getProfile("uid-b")).thenReturn(profileB)

        val resultA = useCase("uid-a") as Resource.Success
        val resultB = useCase("uid-b") as Resource.Success

        assertEquals("Alice", resultA.data!!.name)
        assertEquals("Bob", resultB.data!!.name)
        verify(repository).getProfile("uid-a")
        verify(repository).getProfile("uid-b")
    }
}
