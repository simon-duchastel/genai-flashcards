package data.auth

import domain.model.User
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import kotlin.coroutines.resume
import kotlin.js.Promise

/**
 * Browser/JavaScript implementation of AuthService using popup-based OAuth flow.
 */
class AuthServiceJs(
    private val serverBaseUrl: String = window.location.origin
) : AuthService {

    private val httpClient = HttpClient(Js) {
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

            // Open popup window for OAuth
            val popup = window.open(
                url = authUrl,
                target = "_blank",
                features = "width=500,height=600,popup=yes"
            ) ?: return GoogleSignInResult.Failure("Failed to open authentication window")

            // Wait for OAuth callback
            val authCode = waitForOAuthCallback(popup)

            if (authCode == null) {
                return GoogleSignInResult.Cancelled
            }

            // Exchange code for session token
            val authResponse = authClient.exchangeCodeForToken(authCode)

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
     * Waits for OAuth callback from popup window.
     * Listens for postMessage from callback page with authorization code.
     *
     * @param popup The popup window reference
     * @return Authorization code if successful, null if cancelled/failed
     */
    private suspend fun waitForOAuthCallback(popup: org.w3c.dom.Window): String? =
        suspendCancellableCoroutine { continuation ->
            var resumed = false

            // Listen for messages from popup
            val messageListener: (MessageEvent) -> Unit = { event ->
                if (event.origin == window.location.origin) {
                    val message = event.data.toString()

                    when {
                        message.startsWith("oauth:success:") -> {
                            if (!resumed) {
                                resumed = true
                                val code = message.removePrefix("oauth:success:")
                                continuation.resume(code)
                            }
                        }
                        message == "oauth:cancelled" || message == "oauth:error" -> {
                            if (!resumed) {
                                resumed = true
                                continuation.resume(null)
                            }
                        }
                    }
                }
            }

            window.addEventListener("message", messageListener)

            // Also poll to detect if popup is closed manually
            val checkInterval = window.setInterval({
                if (popup.closed && !resumed) {
                    resumed = true
                    window.removeEventListener("message", messageListener)
                    continuation.resume(null)
                }
            }, 500)

            continuation.invokeOnCancellation {
                window.removeEventListener("message", messageListener)
                window.clearInterval(checkInterval)
                if (!popup.closed) {
                    popup.close()
                }
            }
        }
}

actual fun getAuthService(): AuthService = AuthServiceJs()
