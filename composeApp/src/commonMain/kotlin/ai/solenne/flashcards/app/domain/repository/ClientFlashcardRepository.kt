package ai.solenne.flashcards.app.domain.repository

import ai.solenne.flashcards.app.data.api.ServerFlashcardApiClient
import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import ai.solenne.flashcards.shared.domain.repository.FlashcardRepository

/**
 * Client-side FlashcardRepository implementation that communicates with the server.
 * Requires authentication - all operations need a valid auth token.
 * Used for authenticated users to sync flashcards with the server.
 */
class ClientFlashcardRepository(
    private val authRepository: AuthRepository,
    private val serverClient: ServerFlashcardApiClient
) : FlashcardRepository {

    /**
     * Save a flashcard set to the server.
     * Requires authentication.
     */
    override suspend fun saveFlashcardSet(set: FlashcardSet) {
        val token = authRepository.getSessionToken()
            ?: throw IllegalStateException("Not authenticated")
        serverClient.saveFlashcardSet(token, set)
    }

    /**
     * Get all flashcard sets from the server.
     * Requires authentication.
     */
    override suspend fun getAllFlashcardSets(): List<FlashcardSet> {
        val token = authRepository.getSessionToken() ?: return emptyList()
        return serverClient.getAllFlashcardSets(token) ?: emptyList()
    }

    /**
     * Get a specific flashcard set from the server.
     * Requires authentication.
     */
    override suspend fun getFlashcardSet(id: String): FlashcardSet? {
        val token = authRepository.getSessionToken() ?: return null
        return serverClient.getFlashcardSet(token, id)
    }

    /**
     * Delete a flashcard set from the server.
     * Requires authentication.
     */
    override suspend fun deleteFlashcardSet(id: String) {
        val token = authRepository.getSessionToken()
            ?: throw IllegalStateException("Not authenticated")
        serverClient.deleteFlashcardSet(token, id)
    }

    /**
     * Get randomized flashcards for a set from the server.
     */
    override suspend fun getRandomizedFlashcards(setId: String): List<Flashcard>? {
        return getFlashcardSet(setId)?.flashcards?.shuffled()
    }
}
