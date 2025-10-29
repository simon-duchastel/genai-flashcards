package data.auth

import api.dto.AuthResponse
import api.dto.OAuthPlatform
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
private class AppleAuthenticationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): UIWindow {
        return UIApplication.sharedApplication.keyWindow!!
    }
}

/**
 * iOS implementation of AppleOAuthHandler using ASWebAuthenticationSession.
 */
@OptIn(ExperimentalForeignApi::class)
class SFAppleOAuthHandler(
    private val authApiClient: AuthApiClient
): AppleOAuthHandler {
    /**
     * Start the Apple OAuth flow using ASWebAuthenticationSession.
     * Opens a secure Safari view for user authentication.
     */
    override suspend fun startOAuthFlow(): AuthResponse? {
        return try {
            // Get OAuth URL from server
            val loginUrlResponse = authApiClient.startAppleLogin(platform = OAuthPlatform.IOS)
            val authUrl = loginUrlResponse.authUrl

            // Open ASWebAuthenticationSession for OAuth
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
            println("Apple OAuth flow failed: ${e.message}")
            null
        }
    }

    /**
     * Performs OAuth flow using ASWebAuthenticationSession.
     * Opens a secure Safari view for user authentication.
     *
     * @param authUrl The Apple OAuth authorization URL
     * @return Callback URL if successful, null if cancelled
     */
    private suspend fun performOAuthFlow(authUrl: String): String? =
        suspendCancellableCoroutine { continuation ->
            val url = NSURL.URLWithString(authUrl)
                ?: run {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

            // Use custom URL scheme to intercept the redirect
            val callbackScheme = "solenne-flashcards"

            val authSession = ASWebAuthenticationSession(
                uRL = url,
                callbackURLScheme = callbackScheme
            ) { callbackUrl, error ->
                when {
                    error != null -> {
                        println("Apple OAuth error: $error")
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
            authSession.presentationContextProvider = AppleAuthenticationContextProvider()

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
     * Expected format: solenne-flashcards://callback?auth-redirect=true&token=XXXXX
     *
     * @param callbackUrl The callback URL from ASWebAuthenticationSession
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

actual fun getAppleOAuthHandler(authApiClient: AuthApiClient): AppleOAuthHandler {
    return SFAppleOAuthHandler(authApiClient)
}
