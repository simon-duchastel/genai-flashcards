package com.flashcards.server.storage

import domain.model.FlashcardSet

/**
 * Interface for flashcard set storage.
 */
interface Storage {
    suspend fun save(set: FlashcardSet)
    suspend fun getAll(): List<FlashcardSet>
    suspend fun getById(id: String): FlashcardSet?
    suspend fun delete(id: String)
}
