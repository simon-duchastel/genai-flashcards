package com.flashcards.server.auth

import io.ktor.server.auth.*

/**
 * Principal representing an authenticated user.
 * Contains only the user ID, which can be used to fetch full user data if needed.
 */
data class AuthenticatedUser(
    val userId: String
)
