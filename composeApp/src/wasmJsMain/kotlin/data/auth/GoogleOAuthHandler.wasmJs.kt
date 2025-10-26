package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * WASM-JS implementation of GoogleOAuthHandler using redirect flow.
 */
actual class GoogleOAuthHandler actual constructor(
    private val authApiClient: AuthApiClient
) {
    /**
     * Start the Google OAuth flow by redirecting to Google's auth page.
     * This function never returns - it redirects the entire page.
     */
    actual suspend fun startOAuthFlow(): AuthResponse {
        // Get Google OAuth URL from backend
        val loginUrlResponse = authApiClient.getGoogleLoginUrl()

        // Redirect the entire page to Google's OAuth URL
        // The server will redirect back to /redirect?token=<sessionToken> after successful auth
        window.location.href = loginUrlResponse.authUrl

        delay(1.seconds)
        // This line never executes because we redirect away
        throw IllegalStateException("Redirect should have occurred")
    }
}
