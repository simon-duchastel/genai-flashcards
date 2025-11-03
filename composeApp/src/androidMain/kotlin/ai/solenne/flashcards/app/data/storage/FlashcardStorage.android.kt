package ai.solenne.flashcards.app.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.flashcardDataStore by preferencesDataStore(name = "flashcards")

class FlashcardStorageAndroid(private val context: Context) : FlashcardStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val indexKey = stringPreferencesKey("flashcard_sets_index")

    override suspend fun save(set: FlashcardSet) {
        // Save the flashcard set
        val key = stringPreferencesKey("flashcard_set_${set.id}")
        context.flashcardDataStore.edit { preferences ->
            preferences[key] = json.encodeToString(set)
        }

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.add(set.id)
        context.flashcardDataStore.edit { preferences ->
            preferences[indexKey] = json.encodeToString(currentIndex.toList())
        }
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
        val key = stringPreferencesKey("flashcard_set_$id")
        val jsonString = context.flashcardDataStore.data.map { preferences ->
            preferences[key]
        }.first()

        return jsonString?.let {
            try {
                json.decodeFromString<FlashcardSet>(it)
            } catch (e: Exception) {
                println("Failed to parse flashcard set $id: $e")
                null
            }
        }
    }

    override suspend fun delete(id: String) {
        // Remove the flashcard set
        val key = stringPreferencesKey("flashcard_set_$id")
        context.flashcardDataStore.edit { preferences ->
            preferences.remove(key)
        }

        // Update the index
        val currentIndex = getAllSetIds().toMutableSet()
        currentIndex.remove(id)
        context.flashcardDataStore.edit { preferences ->
            preferences[indexKey] = json.encodeToString(currentIndex.toList())
        }
    }

    private suspend fun getAllSetIds(): List<String> {
        val indexJson = context.flashcardDataStore.data.map { preferences ->
            preferences[indexKey]
        }.first()

        return indexJson?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                println("Failed to parse flashcard set index: $e")
                emptyList()
            }
        } ?: emptyList()
    }
}

private lateinit var flashcardStorageInstance: FlashcardStorage

fun initFlashcardStorage(context: Context) {
    flashcardStorageInstance = FlashcardStorageAndroid(context)
}

actual fun getFlashcardStorage(): FlashcardStorage = flashcardStorageInstance
