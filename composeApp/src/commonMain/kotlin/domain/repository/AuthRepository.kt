package domain.repository

/**
 * Repository for managing authentication state.
 * Provides a clean abstraction over session management.
 */
interface AuthRepository {
    /**
     * Check if user is currently signed in.
     * @return true if user has a valid session, false otherwise
     */
    suspend fun isSignedIn(): Boolean

    /**
     * Get current session token if user is signed in.
     * @return session token or null if not signed in
     */
    suspend fun getSessionToken(): String?
}
