package domain.repository

import data.api.ServerFlashcardApiClient
import data.storage.FlashcardStorage
import domain.generator.FlashcardGenerator
import domain.model.Flashcard
import domain.model.FlashcardSet
import domain.model.FlashcardSetWithMeta
import domain.repository.AuthRepository

/**
 * Repository for managing flashcard sets.
 * Orchestrates between server (for authenticated users) and local storage (for anonymous users).
 * Uses server as source of truth when user is signed in, falls back to local storage otherwise.
 */
class FlashcardRepository(
    private val authRepository: AuthRepository,
    private val serverClient: ServerFlashcardApiClient,
    private val localStorage: FlashcardStorage,
    private val serverGenerator: FlashcardGenerator,
    private val koogGenerator: FlashcardGenerator
) {
    /**
     * Save a flashcard set.
     * If user is signed in: saves to server AND local cache.
     * If user is anonymous: saves to local storage only.
     */
    suspend fun saveFlashcardSet(set: FlashcardSet) {
        if (authRepository.isSignedIn()) {
            val token = authRepository.getSessionToken()
            if (token != null) {
                // Save to server
                serverClient.saveFlashcardSet(token, set)
            } else {
                // Fallback to local if token is missing
                localStorage.saveFlashcardSet(set)
            }
        } else {
            // Anonymous user - save locally only
            localStorage.saveFlashcardSet(set)
        }
    }

    /**
     * Get all flashcard sets.
     * Combines server sets (if signed in) with local-only sets.
     * Server sets are marked as NOT local-only, local-only sets are marked accordingly.
     */
    suspend fun getAllFlashcardSets(): List<FlashcardSetWithMeta> {
        val serverSets = if (authRepository.isSignedIn()) {
            val token = authRepository.getSessionToken()
            if (token != null) {
                serverClient.getAllFlashcardSets(token) ?: emptyList()
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        val localSets = localStorage.getAllFlashcardSets()

        // Determine which local sets are local-only (not on server)
        val serverIds = serverSets.map { it.id }.toSet()
        val localOnlySets = localSets.filter { it.id !in serverIds }

        // Combine: server sets (not local-only) + local-only sets
        val result = serverSets.map { FlashcardSetWithMeta(it, isLocalOnly = false) } +
                     localOnlySets.map { FlashcardSetWithMeta(it, isLocalOnly = true) }

        return result.sortedByDescending { it.flashcardSet.createdAt }
    }

    /**
     * Get a specific flashcard set by ID.
     * Tries server first (if signed in), falls back to local storage.
     */
    suspend fun getFlashcardSet(id: String): FlashcardSet? {
        if (authRepository.isSignedIn()) {
            val token = authRepository.getSessionToken()
            if (token != null) {
                val serverSet = serverClient.getFlashcardSet(token, id)
                if (serverSet != null) {
                    return serverSet
                }
            }
        }

        // Fallback to local storage
        return localStorage.getFlashcardSet(id)
    }

    /**
     * Delete a flashcard set.
     * Deletes from both server (if signed in) and local storage.
     */
    suspend fun deleteFlashcardSet(id: String) {
        if (authRepository.isSignedIn()) {
            val token = authRepository.getSessionToken()
            if (token != null) {
                serverClient.deleteFlashcardSet(token, id)
            }
        }

        // Always delete from local storage to ensure cleanup
        localStorage.deleteFlashcardSet(id)
    }

    /**
     * Get randomized flashcards for a set.
     * Fetches the set and shuffles its flashcards.
     */
    suspend fun getRandomizedFlashcards(setId: String): List<Flashcard>? {
        return getFlashcardSet(setId)?.flashcards?.shuffled()
    }

    /**
     * Generate flashcards using AI.
     * Does NOT auto-save - caller must explicitly save the returned set.
     *
     * @param topic The subject/topic for the flashcards
     * @param count Number of flashcards to generate
     * @param userQuery Additional information to guide generation
     * @return Generated FlashcardSet, or null if generation fails
     */
    suspend fun generate(topic: String, count: Int, userQuery: String): FlashcardSet? {
        val generator = if (authRepository.isSignedIn()) {
            serverGenerator
        } else {
            koogGenerator
        }

        return generator.generate(topic, count, userQuery)
    }
}
