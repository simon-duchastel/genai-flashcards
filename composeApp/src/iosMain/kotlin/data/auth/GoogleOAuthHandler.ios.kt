package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Presentation context provider for ASWebAuthenticationSession.
 */
private class AuthenticationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): UIWindow {
        return UIApplication.sharedApplication.keyWindow!!
    }
}

/**
 * iOS implementation of GoogleOAuthHandler using SFAuthenticationSession.
 */
@OptIn(ExperimentalForeignApi::class)
class SFGoogleOAuthHandler(
    private val authApiClient: AuthApiClient
): GoogleOAuthHandler {
    /**
     * Start the Google OAuth flow using SFAuthenticationSession.
     * Opens a secure Safari view for user authentication.
     */
    override suspend fun startOAuthFlow(): AuthResponse? {
        return try {
            // Get OAuth URL from server
            val loginUrlResponse = authApiClient.startGoogleLogin()
            val authUrl = loginUrlResponse.authUrl

            // Open SFAuthenticationSession for OAuth
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
            println("OAuth flow failed: ${e.message}")
            null
        }
    }

    /**
     * Performs OAuth flow using SFAuthenticationSession.
     * Opens a secure Safari view for user authentication.
     *
     * @param authUrl The Google OAuth authorization URL
     * @return Callback URL if successful, null if cancelled
     */
    private suspend fun performOAuthFlow(authUrl: String): String? =
        suspendCancellableCoroutine { continuation ->
            val url = NSURL.URLWithString(authUrl)
                ?: run {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

            // Use https as the callback scheme to intercept the redirect
            val callbackScheme = "https"

            val authSession = ASWebAuthenticationSession(
                uRL = url,
                callbackURLScheme = callbackScheme
            ) { callbackUrl, error ->
                when {
                    error != null -> {
                        println("OAuth error: $error")
                        continuation.resume(null)
                    }
                    callbackUrl != null -> {
                        continuation.resume(callbackUrl.absoluteString)
                    }
                    else -> {
                        continuation.resume(null)
                    }
                }
            }

            // Set presentation context to use ephemeral session (doesn't save cookies)
            authSession.prefersEphemeralWebBrowserSession = false

            // Set presentation context provider
            authSession.presentationContextProvider = AuthenticationContextProvider()

            // Start the authentication session
            if (authSession.start()) {
                continuation.invokeOnCancellation {
                    authSession.cancel()
                }
            } else {
                continuation.resume(null)
            }
        }

    /**
     * Extracts session token from OAuth callback URL.
     *
     * Expected format: https://flashcards.solenne.ai?auth-redirect=true&token=XXXXX
     *
     * @param callbackUrl The callback URL from SFAuthenticationSession
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

actual fun getGoogleOAuthHandler(authApiClient: AuthApiClient): GoogleOAuthHandler {
    return SFGoogleOAuthHandler(authApiClient)
}
