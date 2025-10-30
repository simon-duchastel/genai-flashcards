package domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Represents an authenticated session.
 *
 * @property sessionToken Cryptographically secure random token (256-bit)
 * @property userId Reference to the user who owns this session
 * @property createdAt Timestamp when session was created
 * @property lastAccessedAt Timestamp of last session access (for idle timeout)
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class Session(
    val sessionToken: String = "",
    val userId: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val lastAccessedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * Check if session has expired.
     */
    fun isExpired(): Boolean {
        return false // for now we don't expire sessions
    }

    /**
     * Create a copy with updated last accessed time.
     */
    fun updateLastAccessed(): Session {
        return copy(lastAccessedAt = Clock.System.now().toEpochMilliseconds())
    }
}
