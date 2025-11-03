package ai.solenne.flashcards.app.data.storage

import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults for persistence.
 */
class FlashcardStorageIos : FlashcardStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val indexKey = "flashcard_sets_index"
    private val userDefaults = NSUserDefaults.standardUserDefaults

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun save(set: FlashcardSet) {
        // Save the flashcard set
        val key = "flashcard_set_${set.id}"
        val jsonString = json.encodeToString(set)
        userDefaults.setObject(jsonString, forKey = key)

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.add(set.id)
        val indexJson = json.encodeToString(currentIndex.toList())
        userDefaults.setObject(indexJson, forKey = indexKey)
        userDefaults.synchronize()
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

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getById(id: String): FlashcardSet? {
        val key = "flashcard_set_$id"
        val jsonString = userDefaults.stringForKey(key) ?: return null
        return try {
            json.decodeFromString<FlashcardSet>(jsonString)
        } catch (e: Exception) {
            println("Failed to parse flashcard set $id: $e")
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun delete(id: String) {
        // Remove the flashcard set
        val key = "flashcard_set_$id"
        userDefaults.removeObjectForKey(key)

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.remove(id)
        val indexJson = json.encodeToString(currentIndex.toList())
        userDefaults.setObject(indexJson, forKey = indexKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getAllSetIds(): List<String> {
        val indexJson = userDefaults.stringForKey(indexKey) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(indexJson)
        } catch (e: Exception) {
            println("Failed to parse flashcard set index: $e")
            emptyList()
        }
    }
}

actual fun getFlashcardStorage(): FlashcardStorage = FlashcardStorageIos()
