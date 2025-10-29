package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient

/**
 * Platform-specific handler for Apple OAuth flow.
 * Uses expect/actual pattern since OAuth implementation differs by platform.
 */
interface AppleOAuthHandler {
    /**
     * Start the Apple OAuth flow.
     * Platform implementations will handle popup/redirect logic.
     */
    suspend fun startOAuthFlow(): AuthResponse?
}

/**
 * Platform-specific factory function for AppleOAuthHandler.
 */
expect fun getAppleOAuthHandler(authApiClient: AuthApiClient): AppleOAuthHandler
