package ai.solenne.flashcards.shared.api.dto

import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
data class RegenerateRequest(
    val flashcardSet: FlashcardSet,
    val regenerationPrompt: String,
)
