package ai.solenne.flashcards.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Raw flashcard without metadata (id, setId, createdAt).
 * Used for AI generation where these fields don't make sense.
 */
@Serializable
data class FlashcardRaw(
    val front: String,
    val back: String
)

/**
 * Raw flashcard set without metadata (id, userId, createdAt).
 * Used for AI generation where these fields are added later.
 */
@Serializable
data class FlashcardSetRaw(
    val topic: String,
    val flashcards: List<FlashcardRaw>
)
