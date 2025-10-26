package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RateLimitError(
    val message: String,
    val tryAgainAt: Long, // Unix timestamp in milliseconds
    val numberOfGenerations: Int
)
