package api.dto

import domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
data class RegenerateRequest(
    val flashcardSet: FlashcardSet,
    val regenerationPrompt: String,
)
