package api.dto

import domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
data class GenerateResponse(
    val flashcardSet: FlashcardSet? = null,
    val error: String? = null
)
