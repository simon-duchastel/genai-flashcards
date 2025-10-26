package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient

/**
 * Platform-specific handler for Google OAuth flow.
 * Uses expect/actual pattern since OAuth implementation differs by platform.
 */
expect class GoogleOAuthHandler(authApiClient: AuthApiClient) {
    /**
     * Start the Google OAuth flow.
     * Platform implementations will handle popup/redirect logic.
     */
    suspend fun startOAuthFlow(): AuthResponse
}
