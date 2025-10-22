package com.flashcards.server.repository

import com.flashcards.server.storage.InMemoryStorage
import domain.model.Flashcard
import domain.model.FlashcardSet
import domain.repository.FlashcardRepository

/**
 * Server-side implementation of FlashcardRepository.
 * Uses in-memory storage for simplicity.
 */
class ServerFlashcardRepository(
    private val storage: InMemoryStorage
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
}
