package data.auth

import domain.model.User

/**
 * Platform-specific authentication service for Google OAuth.
 */
interface AuthService {
    /**
     * Initiates Google Sign-In flow.
     * Opens platform-specific OAuth UI (popup on web, SFAuthenticationSession on iOS).
     *
     * @return GoogleSignInResult indicating success, failure, or cancellation
     */
    suspend fun initiateGoogleSignIn(): GoogleSignInResult

    /**
     * Gets the current authenticated user from the server.
     *
     * @param sessionToken The session token from a successful sign-in
     * @return User if valid session, null if session is invalid/expired
     */
    suspend fun getCurrentUser(sessionToken: String): User?

    /**
     * Logs out the current user by invalidating the session.
     *
     * @param sessionToken The session token to invalidate
     */
    suspend fun logout(sessionToken: String)
}

/**
 * Result of a Google Sign-In attempt.
 */
sealed class GoogleSignInResult {
    /**
     * User successfully authenticated.
     *
     * @param sessionToken Server-issued session token for API calls
     * @param user Authenticated user information
     */
    data class Success(val sessionToken: String, val user: User) : GoogleSignInResult()

    /**
     * Sign-in failed due to an error.
     *
     * @param error Human-readable error message
     */
    data class Failure(val error: String) : GoogleSignInResult()

    /**
     * User cancelled the sign-in flow.
     */
    data object Cancelled : GoogleSignInResult()
}

/**
 * Platform-specific factory function for AuthService.
 */
expect fun getAuthService(): AuthService
