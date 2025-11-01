package domain.repository

import domain.model.Flashcard
import domain.model.FlashcardSet

/**
 * Generator interface for AI-powered flashcard creation.
 * Implementations can use local AI (client-side API) or remote AI (server-side).
 */
interface FlashcardGenerator {
    /**
     * Generate flashcards on a given topic.
     *
     * @param topic The subject/topic for the flashcards
     * @param count Number of flashcards to generate
     * @param userQuery Additional information to guide generation
     * @return FlashcardSet with generated cards, or null if generation fails
     */
    suspend fun generate(topic: String, count: Int, userQuery: String): FlashcardSet?

    /**
     * Regenerate flashcards based on an existing set.
     *
     * @param existingSet The current flashcard set to improve upon
     * @param regenerationPrompt User's custom instruction for regeneration
     * @param count Number of flashcards to generate
     * @return FlashcardSet with regenerated cards, or null if generation fails
     */
    suspend fun regenerate(existingSet: FlashcardSet, regenerationPrompt: String): FlashcardSet?
}
