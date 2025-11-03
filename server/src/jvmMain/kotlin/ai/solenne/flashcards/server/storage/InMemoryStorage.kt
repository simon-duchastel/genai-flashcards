package ai.solenne.flashcards.server.storage

import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple in-memory storage for flashcard sets.
 * Thread-safe using Mutex for concurrent access.
 */
class InMemoryStorage : Storage {
    private val sets = mutableMapOf<String, FlashcardSet>()
    private val mutex = Mutex()

    override suspend fun save(set: FlashcardSet) {
        mutex.withLock {
            sets[set.id] = set
        }
    }

    override suspend fun getAll(): List<FlashcardSet> {
        return mutex.withLock {
            sets.values.toList()
        }
    }

    override suspend fun getById(id: String): FlashcardSet? {
        return mutex.withLock {
            sets[id]
        }
    }

    override suspend fun delete(id: String) {
        mutex.withLock {
            sets.remove(id)
        }
    }
}
