package data.auth

import api.dto.AuthResponse
import api.dto.OAuthProvider
import data.api.AuthApiClient

/**
 * Platform-specific handler for OAuth flow.
 * Uses expect/actual pattern since OAuth implementation differs by platform.
 */
interface OAuthHandler {
    /**
     * Start the OAuth flow for the given provider.
     * Platform implementations will handle popup/redirect logic.
     *
     * @param provider The OAuth provider (GOOGLE, APPLE)
     * @return AuthResponse if successful, null otherwise
     */
    suspend fun startOAuthFlow(provider: OAuthProvider): AuthResponse?
}

/**
 * Platform-specific factory function for OAuthHandler.
 */
expect fun getOAuthHandler(authApiClient: AuthApiClient): OAuthHandler
