package data.storage

import domain.model.FlashcardSet

/**
 * Platform-specific storage for flashcard sets.
 * Uses localStorage on JS/Browser platform.
 */
interface FlashcardStorage {
    suspend fun saveFlashcardSet(set: FlashcardSet)
    suspend fun getAllFlashcardSets(): List<FlashcardSet>
    suspend fun getFlashcardSet(id: String): FlashcardSet?
    suspend fun deleteFlashcardSet(id: String)
}

expect fun getFlashcardStorage(): FlashcardStorage