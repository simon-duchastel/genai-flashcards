package data.auth

import domain.model.User
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.AuthenticationServices.*
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

/**
 * iOS implementation of AuthService using SFAuthenticationSession for OAuth.
 */
@OptIn(ExperimentalForeignApi::class)
class AuthServiceIos(
    private val serverBaseUrl: String = "http://localhost:8080"
) : AuthService {

    private val httpClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val authClient = AuthClient(httpClient, serverBaseUrl)

    override suspend fun initiateGoogleSignIn(): GoogleSignInResult {
        return try {
            // Get OAuth URL from server
            val authUrl = authClient.getGoogleAuthUrl()

            // Open SFAuthenticationSession for OAuth
            val callbackUrl = performOAuthFlow(authUrl)

            if (callbackUrl == null) {
                return GoogleSignInResult.Cancelled
            }

            // Extract authorization code from callback URL
            val code = extractCodeFromCallbackUrl(callbackUrl)
                ?: return GoogleSignInResult.Failure("Failed to extract authorization code")

            // Exchange code for session token
            val authResponse = authClient.exchangeCodeForToken(code)

            GoogleSignInResult.Success(
                sessionToken = authResponse.sessionToken,
                user = authResponse.user
            )
        } catch (e: Exception) {
            GoogleSignInResult.Failure(e.message ?: "Authentication failed")
        }
    }

    override suspend fun getCurrentUser(sessionToken: String): User? {
        return authClient.getCurrentUser(sessionToken)
    }

    override suspend fun logout(sessionToken: String) {
        authClient.logout(sessionToken)
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

            // Callback URL scheme - must match server configuration
            val callbackScheme = "flashcards"

            var authSession: ASWebAuthenticationSession? = null

            authSession = ASWebAuthenticationSession(
                uRL = url,
                callbackURLScheme = callbackScheme
            ) { callbackUrl, error ->
                when {
                    error != null -> {
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

            // Set presentation context provider (required for iOS 13+)
            authSession?.prefersEphemeralWebBrowserSession = false

            // Start the authentication session
            if (authSession?.start() == true) {
                continuation.invokeOnCancellation {
                    authSession?.cancel()
                }
            } else {
                continuation.resume(null)
            }
        }

    /**
     * Extracts authorization code from OAuth callback URL.
     *
     * @param callbackUrl The callback URL from SFAuthenticationSession
     * @return Authorization code if found, null otherwise
     */
    private fun extractCodeFromCallbackUrl(callbackUrl: String): String? {
        // Expected format: flashcards://oauth-callback?code=XXXXX
        val codePrefix = "code="
        val codeIndex = callbackUrl.indexOf(codePrefix)

        if (codeIndex == -1) {
            return null
        }

        val codeStart = codeIndex + codePrefix.length
        val codeEnd = callbackUrl.indexOf('&', codeStart).takeIf { it != -1 }
            ?: callbackUrl.length

        return callbackUrl.substring(codeStart, codeEnd)
    }
}

actual fun getAuthService(): AuthService = AuthServiceIos()
