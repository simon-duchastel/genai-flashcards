package data.auth

import api.dto.AuthResponse
import api.dto.LoginUrlResponse
import api.dto.MeResponse
import api.routes.ApiRoutes
import domain.model.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Client for making authentication-related API calls to the server.
 */
class AuthClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    /**
     * Gets the Google OAuth authorization URL from the server.
     */
    suspend fun getGoogleAuthUrl(): String {
        val response = httpClient.get(baseUrl + ApiRoutes.AUTH_GOOGLE_LOGIN)
        val loginUrlResponse: LoginUrlResponse = response.body()
        return loginUrlResponse.authUrl
    }

    /**
     * Exchanges an authorization code for a session token and user info.
     *
     * @param code The authorization code from Google OAuth callback
     * @return AuthResponse containing session token and user
     */
    suspend fun exchangeCodeForToken(code: String): AuthResponse {
        val response = httpClient.get(baseUrl + ApiRoutes.AUTH_GOOGLE_CALLBACK) {
            parameter("code", code)
        }
        return response.body()
    }

    /**
     * Gets the current authenticated user.
     *
     * @param sessionToken The session token
     * @return User if session is valid, null otherwise
     */
    suspend fun getCurrentUser(sessionToken: String): User? {
        return try {
            val response = httpClient.get(baseUrl + ApiRoutes.AUTH_ME) {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
            val meResponse: MeResponse = response.body()
            meResponse.user
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Logs out by invalidating the session token.
     *
     * @param sessionToken The session token to invalidate
     */
    suspend fun logout(sessionToken: String) {
        try {
            httpClient.post(baseUrl + ApiRoutes.AUTH_LOGOUT) {
                header(HttpHeaders.Authorization, "Bearer $sessionToken")
            }
        } catch (e: Exception) {
            // Ignore errors on logout
        }
    }
}
