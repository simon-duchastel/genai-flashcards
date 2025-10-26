package domain.model

/**
 * Wrapper around FlashcardSet with additional metadata for UI display.
 */
data class FlashcardSetWithMeta(
    val flashcardSet: FlashcardSet,
    val isLocalOnly: Boolean
)
