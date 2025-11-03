package ai.solenne.flashcards.app.data.auth

import ai.solenne.flashcards.shared.api.dto.AuthResponse
import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import ai.solenne.flashcards.app.data.api.AuthApiClient

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
