package data.storage

import domain.model.FlashcardSet
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JavaScript/Browser implementation using localStorage API.
 */
actual class FlashcardStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val indexKey = "flashcard_sets_index"

    actual suspend fun saveFlashcardSet(set: FlashcardSet) {
        // Save the flashcard set
        val key = "flashcard_set_${set.id}"
        localStorage.setItem(key, json.encodeToString(set))

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.add(set.id)
        localStorage.setItem(indexKey, json.encodeToString(currentIndex.toList()))
    }

    actual suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        val setIds = getAllSetIds()
        return setIds.mapNotNull { id ->
            try {
                getFlashcardSet(id)
            } catch (e: Exception) {
                console.error("Failed to load flashcard set $id", e)
                null
            }
        }
    }

    actual suspend fun getFlashcardSet(id: String): FlashcardSet? {
        val key = "flashcard_set_$id"
        val jsonString = localStorage.getItem(key) ?: return null
        return try {
            json.decodeFromString<FlashcardSet>(jsonString)
        } catch (e: Exception) {
            console.error("Failed to parse flashcard set $id", e)
            null
        }
    }

    actual suspend fun deleteFlashcardSet(id: String) {
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
            console.error("Failed to parse flashcard set index", e)
            emptyList()
        }
    }
}
