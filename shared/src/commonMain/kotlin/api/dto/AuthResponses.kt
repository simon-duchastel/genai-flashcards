package api.dto

import domain.model.User
import kotlinx.serialization.Serializable

/**
 * Response containing Google OAuth login URL.
 */
@Serializable
data class LoginUrlResponse(
    val authUrl: String
)

/**
 * Response after successful authentication.
 */
@Serializable
data class AuthResponse(
    val sessionToken: String,
    val user: User
)

/**
 * Response for the /auth/me endpoint.
 */
@Serializable
data class MeResponse(
    val user: User
)
