package ai.solenne.flashcards.app.domain.model

import ai.solenne.flashcards.shared.domain.model.FlashcardSet

/**
 * Wrapper around FlashcardSet with additional metadata for UI display.
 */
data class FlashcardSetWithMeta(
    val flashcardSet: FlashcardSet,
    val isLocalOnly: Boolean
)
