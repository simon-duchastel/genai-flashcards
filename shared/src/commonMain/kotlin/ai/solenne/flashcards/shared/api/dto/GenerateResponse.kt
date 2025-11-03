package ai.solenne.flashcards.shared.api.dto

import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
data class GenerateResponse(
    val flashcardSet: FlashcardSet? = null,
    val error: String? = null
)
