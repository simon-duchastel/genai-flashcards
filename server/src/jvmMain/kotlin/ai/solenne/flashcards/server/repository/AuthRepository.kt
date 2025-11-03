package ai.solenne.flashcards.server.repository

import ai.solenne.flashcards.shared.domain.model.Session
import ai.solenne.flashcards.shared.domain.model.User

/**
 * Repository interface for authentication and user management.
 * Allows for different implementations (in-memory, PostgreSQL, etc.)
 */
interface AuthRepository {
    /**
     * Create a new session for a user.
     *
     * @param userId The user ID to create a session for
     * @return The created session
     */
    suspend fun createSession(userId: String): Session

    /**
     * Get a session by its token.
     *
     * @param token The session token
     * @return The session if found and valid, null otherwise
     */
    suspend fun getSession(token: String): Session?

    /**
     * Invalidate a session (logout).
     *
     * @param token The session token to invalidate
     */
    suspend fun invalidateSession(token: String)

    /**
     * Get a user by their OAuth provider ID (authId).
     *
     * @param authId The OAuth provider's user identifier
     * @return The user if found, null otherwise
     */
    suspend fun getUserByAuthId(authId: String): User?

    /**
     * Get a user by their internal user ID.
     *
     * @param userId The internal user ID
     * @return The user if found, null otherwise
     */
    suspend fun getUserById(userId: String): User?

    /**
     * Create a new user.
     *
     * @param user The user to create
     * @return The created user
     */
    suspend fun createUser(user: User): User

    /**
     * Update last accessed time for a session.
     *
     * @param token The session token
     */
    suspend fun updateSessionAccess(token: String)

    /**
     * Delete a user account and all associated data.
     * This will delete:
     * - All sessions for the user
     * - The authId -> userId mapping
     * - The user document
     *
     * Note: Flashcard sets must be deleted separately via Storage.deleteAllByUserId()
     *
     * @param userId The user ID to delete
     */
    suspend fun deleteUserAccount(userId: String)
}
