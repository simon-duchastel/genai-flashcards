package com.flashcards.server.storage

/**
 * Interface for rate limiting flashcard generation.
 */
interface RateLimiter {
    /**
     * Set a custom rate limit for a specific user.
     */
    suspend fun setUserLimit(userId: String, limit: Int)

    /**
     * Get the rate limit for a specific user.
     */
    suspend fun getUserLimit(userId: String): Int

    /**
     * Check if a user can generate flashcards.
     */
    suspend fun checkRateLimit(userId: String): RateLimitResult

    /**
     * Record a generation attempt for a user.
     */
    suspend fun recordAttempt(userId: String)
}
