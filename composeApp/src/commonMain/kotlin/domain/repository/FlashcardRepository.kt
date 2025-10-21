package domain.repository

import data.storage.FlashcardStorage
import domain.model.FlashcardSet

/**
 * Repository for managing flashcard sets.
 * Provides a clean API over the storage layer.
 */
class FlashcardRepository(
    private val storage: FlashcardStorage
) {
    suspend fun saveFlashcardSet(set: FlashcardSet) {
        storage.saveFlashcardSet(set)
    }

    suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        return storage.getAllFlashcardSets()
            .sortedByDescending { it.createdAt }
    }

    suspend fun getFlashcardSet(id: String): FlashcardSet? {
        return storage.getFlashcardSet(id)
    }

    suspend fun deleteFlashcardSet(id: String) {
        storage.deleteFlashcardSet(id)
    }

    suspend fun getRandomizedFlashcards(setId: String): List<domain.model.Flashcard>? {
        return storage.getFlashcardSet(setId)?.flashcards?.shuffled()
    }
}
