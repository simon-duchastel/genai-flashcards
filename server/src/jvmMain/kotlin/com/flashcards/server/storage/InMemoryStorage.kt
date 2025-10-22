package com.flashcards.server.storage

import domain.model.FlashcardSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple in-memory storage for flashcard sets.
 * Thread-safe using Mutex for concurrent access.
 */
class InMemoryStorage {
    private val sets = mutableMapOf<String, FlashcardSet>()
    private val mutex = Mutex()

    suspend fun save(set: FlashcardSet) {
        mutex.withLock {
            sets[set.id] = set
        }
    }

    suspend fun getAll(): List<FlashcardSet> {
        return mutex.withLock {
            sets.values.toList()
        }
    }

    suspend fun getById(id: String): FlashcardSet? {
        return mutex.withLock {
            sets[id]
        }
    }

    suspend fun delete(id: String) {
        mutex.withLock {
            sets.remove(id)
        }
    }
}
