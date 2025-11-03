package ai.solenne.flashcards.app.data.auth

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import ai.solenne.flashcards.shared.api.dto.AuthResponse
import ai.solenne.flashcards.shared.api.dto.OAuthPlatform
import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import ai.solenne.flashcards.app.data.api.AuthApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Android implementation of OAuthHandler using Chrome Custom Tabs.
 * Handles both Google and Apple OAuth flows.
 */
class ChromeCustomTabsOAuthHandler(
    private val context: Context,
    private val authApiClient: AuthApiClient
) : OAuthHandler {

    companion object {
        private const val OAUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes

        @Volatile
        private var currentContinuation: Continuation<String?>? = null

        /**
         * Called by MainActivity when OAuth callback is received.
         */
        fun handleOAuthCallback(callbackUrl: String) {
            currentContinuation?.resume(callbackUrl)
            currentContinuation = null
        }
    }

    /**
     * Start the OAuth flow using Chrome Custom Tabs.
     * Opens a secure browser tab for user authentication.
     *
     * @param provider The OAuth provider (GOOGLE or APPLE)
     */
    override suspend fun startOAuthFlow(provider: OAuthProvider): AuthResponse? {
        return try {
            // Get OAuth URL from server based on provider
            val loginUrlResponse = when (provider) {
                OAuthProvider.GOOGLE -> authApiClient.startGoogleLogin(platform = OAuthPlatform.ANDROID)
                OAuthProvider.APPLE -> authApiClient.startAppleLogin(platform = OAuthPlatform.ANDROID)
            }
            val authUrl = loginUrlResponse.authUrl

            // Open Chrome Custom Tab for OAuth
            val callbackUrl = performOAuthFlow(authUrl)
                ?: return null

            // Extract session token from callback URL
            val token = extractTokenFromCallbackUrl(callbackUrl)
                ?: return null

            // Get user info from server
            val meResponse = authApiClient.getMe(token)

            // Return AuthResponse
            AuthResponse(
                sessionToken = token,
                user = meResponse.user
            )
        } catch (e: Exception) {
            println("OAuth flow failed for $provider: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Performs OAuth flow using Chrome Custom Tabs.
     * Opens a secure browser tab for user authentication.
     *
     * @param authUrl The OAuth authorization URL
     * @return Callback URL if successful, null if cancelled or timed out
     */
    private suspend fun performOAuthFlow(authUrl: String): String? =
        withTimeoutOrNull(OAUTH_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                // Store continuation for callback
                currentContinuation = continuation

                // Launch Chrome Custom Tab
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()

                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    customTabsIntent.launchUrl(context, authUrl.toUri())

                    continuation.invokeOnCancellation {
                        currentContinuation = null
                    }
                } catch (e: Exception) {
                    println("Failed to launch Chrome Custom Tab: ${e.message}")
                    currentContinuation = null
                    continuation.resume(null)
                }
            }
        }

    /**
     * Extracts session token from OAuth callback URL.
     *
     * Expected format: solenne-flashcards://callback?auth-redirect=true&token=XXXXX
     *
     * @param callbackUrl The callback URL from Chrome Custom Tab
     * @return Session token if found, null otherwise
     */
    private fun extractTokenFromCallbackUrl(callbackUrl: String): String? {
        val tokenPrefix = "token="
        val tokenIndex = callbackUrl.indexOf(tokenPrefix)

        if (tokenIndex == -1) {
            return null
        }

        val tokenStart = tokenIndex + tokenPrefix.length
        val tokenEnd = callbackUrl.indexOf('&', tokenStart).takeIf { it != -1 }
            ?: callbackUrl.length

        return callbackUrl.substring(tokenStart, tokenEnd)
    }
}
