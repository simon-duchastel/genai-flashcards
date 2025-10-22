package domain.repository

import data.storage.FlashcardStorage
import domain.model.FlashcardSet

/**
 * Repository for managing flashcard sets.
 * Provides a clean API over the storage layer.
 */
class FlashcardRepositoryImpl(
    private val storage: FlashcardStorage
) : FlashcardRepository {
    override suspend fun saveFlashcardSet(set: FlashcardSet) {
        storage.saveFlashcardSet(set)
    }

    override suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        return storage.getAllFlashcardSets()
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getFlashcardSet(id: String): FlashcardSet? {
        return storage.getFlashcardSet(id)
    }

    override suspend fun deleteFlashcardSet(id: String) {
        storage.deleteFlashcardSet(id)
    }

    override suspend fun getRandomizedFlashcards(setId: String): List<domain.model.Flashcard>? {
        return storage.getFlashcardSet(setId)?.flashcards?.shuffled()
    }
}
