package domain.model

import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Represents a user in the system.
 *
 * @property userId Internal user identifier (UUID)
 * @property authId External OAuth provider identifier (e.g., Google's 'sub' claim)
 * @property createdAt Timestamp when user was created
 */
@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Serializable
data class User(
    val userId: String = Uuid.random().toString(),
    val authId: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
