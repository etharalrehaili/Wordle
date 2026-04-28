package com.khammin.game.domain.usecases.game

import android.util.Log
import com.khammin.core.util.normalizeForWordle
import com.khammin.game.domain.repository.GameRepository
import javax.inject.Inject

class ValidateWordUseCase @Inject constructor(
    private val repo: GameRepository
) {
    /**
     * Returns true if the word is valid.
     * Checks the local Strapi word list first; only calls the Claude endpoint
     * when the word is not found locally. Defaults to false on any error.
     */
    suspend operator fun invoke(word: String, language: String, localWordList: List<String>): Boolean {
        val normalisedLower  = word.trim().lowercase()
        val normalisedWordle = word.normalizeForWordle()

        Log.d("WordValidation", "──────────────────────────────────────────")
        Log.d("WordValidation", "INPUT       raw='$word'")
        Log.d("WordValidation", "INPUT       trim+lower='$normalisedLower'  normalizeForWordle='$normalisedWordle'")
        Log.d("WordValidation", "WORD_LIST   size=${localWordList.size}  language=$language")

        // Current check (trim+lowercase — may miss Arabic char variants)
        val matchedByLower = localWordList.firstOrNull { it.trim().lowercase() == normalisedLower }
        // Correct check (full normalizeForWordle — folds أ/إ/آ → ا and uppercases)
        val matchedByWordle = localWordList.firstOrNull { it.normalizeForWordle() == normalisedWordle }

        Log.d("WordValidation", "LOCAL_CHECK trim+lower  → match=${matchedByLower != null}  entry='$matchedByLower'")
        Log.d("WordValidation", "LOCAL_CHECK normalizeForWordle → match=${matchedByWordle != null}  entry='$matchedByWordle'")

        if (matchedByLower != null) {
            Log.d("WordValidation", "RESULT      valid=true  source=local(trim+lower)")
            return true
        }

        if (matchedByLower == null && matchedByWordle != null) {
            Log.w("WordValidation", "MISMATCH    word is in local list via normalizeForWordle but NOT via trim+lower — " +
                    "falling through to Claude unnecessarily! raw='$word'  stored='$matchedByWordle'")
        }

        Log.d("WordValidation", "LOCAL_CHECK miss → calling Claude API  word='$word'  language=$language")
        return try {
            val result = repo.validateWord(word, language)
            Log.d("WordValidation", "CLAUDE_API  word='$word'  isValid=$result")
            if (!result && matchedByWordle != null) {
                Log.e("WordValidation", "FALSE_NEGATIVE  Claude rejected '$word' but it IS in local list (normalizeForWordle match='$matchedByWordle')")
            }
            result
        } catch (e: Exception) {
            Log.e("WordValidation", "CLAUDE_API  ERROR word='$word'  exception=${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }
}
