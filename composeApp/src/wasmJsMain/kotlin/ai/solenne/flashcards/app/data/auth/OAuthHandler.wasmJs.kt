package ai.solenne.flashcards.app.data.auth

import ai.solenne.flashcards.shared.api.dto.AuthResponse
import ai.solenne.flashcards.shared.api.dto.OAuthPlatform
import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import ai.solenne.flashcards.app.data.api.AuthApiClient
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Web implementation of OAuthHandler.
 * Currently not implemented - OAuth for web requires different setup.
 * For MVP, we focus on iOS implementation only.
 */
class WebOAuthHandler(
    private val authApiClient: AuthApiClient
): OAuthHandler {
    /**
     * Start the Google OAuth flow by redirecting to Google's auth page.
     * This function never returns - it's expected to redirect the entire page.
     */
    override suspend fun startOAuthFlow(provider: OAuthProvider): AuthResponse? {
        val loginUrl = when (provider){
            OAuthProvider.GOOGLE -> authApiClient.startGoogleLogin(OAuthPlatform.WEB)
            OAuthProvider.APPLE -> authApiClient.startAppleLogin(OAuthPlatform.WEB)
        } .authUrl

        window.location.href = loginUrl // redirect to the login url

        delay(1.seconds)
        return null // redirect should have occurred, return an error if it hasn't
    }
}

