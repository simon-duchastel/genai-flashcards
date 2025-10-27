package com.flashcards.server.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Result of a rate limit check.
 */
sealed class RateLimitResult {
    /**
     * Rate limit check passed - user can proceed with generation.
     */
    data object Ok : RateLimitResult()

    /**
     * Rate limit exceeded - user must wait before generating again.
     */
    data class RateLimitExceeded(
        val tryAgainAt: Long, // Unix timestamp in milliseconds
        val numberOfGenerations: Int
    ) : RateLimitResult()
}

/**
 * Rate limiter for tracking user flashcard generation attempts.
 * Used for rate limiting to prevent abuse.
 */
class GenerationRateLimiter : RateLimiter {
    private data class GenerationAttempt(
        val userId: String,
        val timestamp: Instant
    )

    private val attempts = mutableListOf<GenerationAttempt>()
    private val userLimits = mutableMapOf<String, Int>() // userId -> max generations per 24h
    private val mutex = Mutex()

    companion object {
        private const val DEFAULT_RATE_LIMIT = 20
    }

    /**
     * Set a custom rate limit for a specific user.
     * If not set, the default rate limit of 20 will be used.
     */
    override suspend fun setUserLimit(userId: String, limit: Int) {
        mutex.withLock {
            userLimits[userId] = limit
        }
    }

    /**
     * Get the rate limit for a specific user.
     * Returns the custom limit if set, otherwise the default of 20.
     */
    override suspend fun getUserLimit(userId: String): Int {
        return mutex.withLock {
            userLimits[userId] ?: DEFAULT_RATE_LIMIT
        }
    }

    /**
     * Check if a user can generate flashcards.
     * Returns RateLimitResult.Ok if allowed, or RateLimitResult.RateLimitExceeded if not.
     */
    override suspend fun checkRateLimit(userId: String): RateLimitResult {
        return mutex.withLock {
            val cutoff = Clock.System.now() - 24.hours
            val userLimit = userLimits[userId] ?: DEFAULT_RATE_LIMIT

            // Remove attempts older than 24 hours
            attempts.removeAll { it.timestamp < cutoff }

            // Get recent attempts for this user
            val recentAttempts = attempts
                .filter { it.userId == userId && it.timestamp >= cutoff }
                .sortedByDescending { it.timestamp }

            if (recentAttempts.size >= userLimit) {
                // Rate limit exceeded
                val earliestAttempt = recentAttempts.last().timestamp
                val tryAgainAt = earliestAttempt + 24.hours

                RateLimitResult.RateLimitExceeded(
                    tryAgainAt = tryAgainAt.toEpochMilliseconds(),
                    numberOfGenerations = recentAttempts.size
                )
            } else {
                RateLimitResult.Ok
            }
        }
    }

    /**
     * Record a generation attempt for a user.
     * Should be called after a successful generation.
     */
    override suspend fun recordAttempt(userId: String) {
        mutex.withLock {
            attempts.add(GenerationAttempt(userId, Clock.System.now()))
        }
    }
}
