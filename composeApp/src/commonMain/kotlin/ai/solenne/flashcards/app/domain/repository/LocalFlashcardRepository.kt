package ai.solenne.flashcards.app.domain.repository

import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import ai.solenne.flashcards.shared.domain.repository.FlashcardRepository
import ai.solenne.flashcards.shared.domain.storage.Storage

/**
 * Local FlashcardRepository implementation for offline/anonymous use.
 * Uses local storage (IndexedDB, localStorage, DataStore, etc.) to persist flashcards.
 * Does not require authentication.
 */
class LocalFlashcardRepository(
    private val storage: Storage
) : FlashcardRepository {

    /**
     * Save a flashcard set to local storage.
     */
    override suspend fun saveFlashcardSet(set: FlashcardSet) {
        storage.save(set)
    }

    /**
     * Get all flashcard sets from local storage.
     */
    override suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        return storage.getAll().sortedByDescending { it.createdAt }
    }

    /**
     * Get a specific flashcard set from local storage.
     */
    override suspend fun getFlashcardSet(id: String): FlashcardSet? {
        return storage.getById(id)
    }

    /**
     * Delete a flashcard set from local storage.
     */
    override suspend fun deleteFlashcardSet(id: String) {
        storage.delete(id)
    }

    /**
     * Get randomized flashcards for a set.
     */
    override suspend fun getRandomizedFlashcards(setId: String): List<Flashcard>? {
        return getFlashcardSet(setId)?.flashcards?.shuffled()
    }
}
