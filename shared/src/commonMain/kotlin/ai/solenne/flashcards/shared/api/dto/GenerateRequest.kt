package ai.solenne.flashcards.shared.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val topic: String,
    val count: Int,
    val userQuery: String,
)
