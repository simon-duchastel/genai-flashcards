package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient

/**
 * Platform-specific handler for Google OAuth flow.
 * Uses expect/actual pattern since OAuth implementation differs by platform.
 */
interface GoogleOAuthHandler {
    /**
     * Start the Google OAuth flow.
     * Platform implementations will handle popup/redirect logic.
     */
    suspend fun startOAuthFlow(): AuthResponse?
}

/**
 * Platform-specific factory function for GoogleOAuthHandler.
 */
expect fun getGoogleOAuthHandler(authApiClient: AuthApiClient): GoogleOAuthHandler
