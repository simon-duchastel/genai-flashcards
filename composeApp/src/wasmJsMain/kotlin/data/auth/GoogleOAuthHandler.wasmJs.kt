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
     * This function never returns - it's expected to redirect the entire page.
     */
    actual suspend fun startOAuthFlow(): AuthResponse? {
        val loginUrl = authApiClient.startGoogleLogin().authUrl

        window.location.href = loginUrl // redirect to the login url

        delay(1.seconds)
        return null // redirect should have occurred, return an error if it hasn't
    }
}
