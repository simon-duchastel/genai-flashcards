package domain.repository

import domain.model.Flashcard
import domain.model.FlashcardSet

/**
 * Repository interface for managing flashcard sets.
 * Implementations can be local (in-browser/in-app) or remote (server-based).
 */
interface FlashcardRepository {
    /**
     * Save a flashcard set.
     */
    suspend fun saveFlashcardSet(set: FlashcardSet)

    /**
     * Get all flashcard sets, sorted by creation date (newest first).
     */
    suspend fun getAllFlashcardSets(): List<FlashcardSet>

    /**
     * Get a specific flashcard set by ID.
     */
    suspend fun getFlashcardSet(id: String): FlashcardSet?

    /**
     * Delete a flashcard set by ID.
     */
    suspend fun deleteFlashcardSet(id: String)

    /**
     * Get randomized flashcards for a specific set.
     */
    suspend fun getRandomizedFlashcards(setId: String): List<Flashcard>?
}
