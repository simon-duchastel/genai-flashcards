package domain.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Represents a user in the system.
 *
 * @property userId Internal user identifier (UUID)
 * @property authId External OAuth provider identifier (e.g., Google's 'sub' claim)
 * @property email User's email address from OAuth provider
 * @property name User's display name
 * @property picture URL to user's profile picture
 * @property createdAt Timestamp when user was created
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class User(
    val userId: String = Uuid.random().toString(),
    val authId: String,
    val email: String,
    val name: String? = null,
    val picture: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
