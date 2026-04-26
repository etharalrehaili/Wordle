package com.khammin.game.domain.repository

import com.khammin.game.domain.model.ChallengeSnapshot
import kotlinx.coroutines.flow.Flow

interface ChallengeProgressRepository {
    /** Real-time stream of the user's full challenge snapshot. */
    fun observeSnapshot(uid: String): Flow<ChallengeSnapshot>

    /** One-shot read for use during evaluation — no listener overhead. */
    suspend fun getSnapshot(uid: String): ChallengeSnapshot

    /** Creates the progress document with all challenges at AVAILABLE if it doesn't exist yet. */
    suspend fun initializeIfNeeded(uid: String)

    /** Persists the full snapshot back to Firestore. */
    suspend fun saveSnapshot(uid: String, snapshot: ChallengeSnapshot)
}
