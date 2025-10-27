package com.flashcards.server.storage

import com.flashcards.server.util.await
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

/**
 * Firestore-backed rate limiter for tracking user flashcard generation attempts.
 * Uses Firestore for persistent storage with caching for performance.
 *
 * Collections:
 * - rate_limit_attempts/{userId}_{timestampMs}: Generation attempt documents
 * - rate_limit_configs/{userId}: Custom user limit configurations
 *
 * Caching strategy:
 * - Rate limit check results cached for 1 minute per user
 * - Reduces Firestore reads on repeated checks
 */
class FirestoreRateLimiter(
    private val firestore: Firestore
) : RateLimiter {
    private val attemptsCollection = firestore.collection("rate_limit_attempts")
    private val configsCollection = firestore.collection("rate_limit_configs")

    companion object {
        private const val DEFAULT_RATE_LIMIT = 20
    }

    // Cache rate limit check results for 1 minute
    // Key: userId, Value: RateLimitResult
    private val rateLimitCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<String, RateLimitResult>()

    /**
     * Set a custom rate limit for a specific user.
     * If not set, the default rate limit of 20 will be used.
     */
    override suspend fun setUserLimit(userId: String, limit: Int) {
        configsCollection
            .document(userId)
            .set(mapOf("customLimit" to limit))
            .await()

        // Invalidate cache for this user
        rateLimitCache.invalidate(userId)
    }

    /**
     * Get the rate limit for a specific user.
     * Returns the custom limit if set, otherwise the default of 20.
     */
    override suspend fun getUserLimit(userId: String): Int {
        val docSnapshot = configsCollection
            .document(userId)
            .get()
            .await()

        if (!docSnapshot.exists()) {
            return DEFAULT_RATE_LIMIT
        }

        return (docSnapshot.get("customLimit") as? Long)?.toInt() ?: DEFAULT_RATE_LIMIT
    }

    /**
     * Check if a user can generate flashcards.
     * Returns RateLimitResult.Ok if allowed, or RateLimitResult.RateLimitExceeded if not.
     */
    override suspend fun checkRateLimit(userId: String): RateLimitResult {
        rateLimitCache.getIfPresent(userId)?.let { cachedResult ->
            return cachedResult
        }

        // Cache miss - perform actual check
        val cutoff = Clock.System.now() - 24.hours
        val userLimit = getUserLimit(userId)

        // Query attempts in last 24 hours for this user
        // Document IDs are formatted as: {userId}_{timestampMs}
        // We query by field since we need to filter by both userId and timestamp
        val querySnapshot = attemptsCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", cutoff.toEpochMilliseconds())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val recentAttempts = querySnapshot.documents

        val result = if (recentAttempts.size >= userLimit) {
            // Rate limit exceeded
            val earliestTimestamp = recentAttempts
                .mapNotNull { it.get("timestamp") as? Long }
                .minOrNull() ?: cutoff.toEpochMilliseconds()

            val tryAgainAt = earliestTimestamp + 24.hours.inWholeMilliseconds

            RateLimitResult.RateLimitExceeded(
                tryAgainAt = tryAgainAt,
                numberOfGenerations = recentAttempts.size
            )
        } else {
            RateLimitResult.Ok
        }

        // Cache the result
        rateLimitCache.put(userId, result)

        return result
    }

    /**
     * Record a generation attempt for a user.
     * Should be called after a successful generation.
     */
    override suspend fun recordAttempt(userId: String) {
        val now = Clock.System.now()
        val timestamp = now.toEpochMilliseconds()

        // Document ID: userId_timestamp for easy querying and uniqueness
        val docId = "${userId}_${timestamp}"

        attemptsCollection
            .document(docId)
            .set(mapOf(
                "userId" to userId,
                "timestamp" to timestamp
            ))
            .await()

        // Invalidate cache for this user (their limit status changed)
        rateLimitCache.invalidate(userId)
    }

    /**
     * Clean up old attempts (older than 24 hours).
     * Should be called periodically to prevent unbounded growth.
     *
     * Note: In production, this should be done via a scheduled Cloud Function
     * or similar background job, not on every request.
     */
    suspend fun cleanupOldAttempts() {
        val cutoff = Clock.System.now() - 24.hours

        val querySnapshot = attemptsCollection
            .whereLessThan("timestamp", cutoff.toEpochMilliseconds())
            .get()
            .await()

        // Delete in batches (Firestore batch limit is 500)
        var batch = firestore.batch()
        var count = 0

        querySnapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
            count++

            if (count >= 500) {
                batch.commit().await()
                batch = firestore.batch()
                count = 0
            }
        }

        if (count > 0) {
            batch.commit().await()
        }
    }
}
