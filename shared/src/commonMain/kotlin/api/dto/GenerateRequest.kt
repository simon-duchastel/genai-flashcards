package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val topic: String,
    val count: Int,
    val userQuery: String,
    val apiKey: String? = null  // Client can provide their own Gemini API key
)
