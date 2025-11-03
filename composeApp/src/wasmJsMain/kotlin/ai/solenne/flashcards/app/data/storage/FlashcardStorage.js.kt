package ai.solenne.flashcards.app.data.storage

import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class FlashcardStorageJs: FlashcardStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val indexKey = "flashcard_sets_index"

    override suspend fun save(set: FlashcardSet) {
        // Save the flashcard set
        val key = "flashcard_set_${set.id}"
        localStorage.setItem(key, json.encodeToString(set))

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.add(set.id)
        localStorage.setItem(indexKey, json.encodeToString(currentIndex.toList()))
    }

    override suspend fun getAll(): List<FlashcardSet> {
        val setIds = getAllSetIds()
        return setIds.mapNotNull { id ->
            try {
                getById(id)
            } catch (e: Exception) {
                println("Failed to load flashcard set $id: $e")
                null
            }
        }
    }

    override suspend fun getById(id: String): FlashcardSet? {
        val key = "flashcard_set_$id"
        val jsonString = localStorage.getItem(key) ?: return null
        return try {
            json.decodeFromString<FlashcardSet>(jsonString)
        } catch (e: Exception) {
            println("Failed to parse flashcard set $id: $e")
            null
        }
    }

    override suspend fun delete(id: String) {
        // Remove the flashcard set
        val key = "flashcard_set_$id"
        localStorage.removeItem(key)

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.remove(id)
        localStorage.setItem(indexKey, json.encodeToString(currentIndex.toList()))
    }

    private fun getAllSetIds(): List<String> {
        val indexJson = localStorage.getItem(indexKey) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(indexJson)
        } catch (e: Exception) {
            println("Failed to parse flashcard set index: $e")
            emptyList()
        }
    }
}

actual fun getFlashcardStorage(): FlashcardStorage = FlashcardStorageJs()