package ai.solenne.flashcards.server.repository

import ai.solenne.flashcards.server.storage.Storage
import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import ai.solenne.flashcards.shared.domain.repository.FlashcardRepository

/**
 * Server-side implementation of FlashcardRepository.
 * Uses storage abstraction for flexibility (in-memory or Firestore).
 *
 * All methods now require userId for data isolation.
 */
class ServerFlashcardRepository(
    private val storage: Storage
) : FlashcardRepository {

    override suspend fun saveFlashcardSet(set: FlashcardSet) {
        storage.save(set)
    }

    override suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        return storage.getAll()
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getFlashcardSet(id: String): FlashcardSet? {
        return storage.getById(id)
    }

    override suspend fun deleteFlashcardSet(id: String) {
        storage.delete(id)
    }

    override suspend fun getRandomizedFlashcards(setId: String): List<Flashcard>? {
        return storage.getById(setId)?.flashcards?.shuffled()
    }

    // User-scoped methods for authenticated access
    suspend fun getAllFlashcardSets(userId: String): List<FlashcardSet> {
        return storage.getAll()
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
    }

    suspend fun getFlashcardSet(id: String, userId: String): FlashcardSet? {
        val set = storage.getById(id)
        return if (set?.userId == userId) set else null
    }

    suspend fun deleteFlashcardSet(id: String, userId: String) {
        val set = storage.getById(id)
        if (set?.userId == userId) {
            storage.delete(id)
        }
    }

    suspend fun getRandomizedFlashcards(setId: String, userId: String): List<Flashcard>? {
        val set = storage.getById(setId)
        return if (set?.userId == userId) set.flashcards.shuffled() else null
    }
}
