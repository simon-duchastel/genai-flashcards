package domain.storage

import domain.model.FlashcardSet

/**
 * Interface for flashcard set storage.
 * Used by both server (Firestore) and client (IndexedDB/localStorage) implementations.
 */
interface Storage {
    suspend fun save(set: FlashcardSet)
    suspend fun getAll(): List<FlashcardSet>
    suspend fun getById(id: String): FlashcardSet?
    suspend fun delete(id: String)
}
