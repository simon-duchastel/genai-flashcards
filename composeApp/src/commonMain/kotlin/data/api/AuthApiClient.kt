package data.api

import api.dto.LoginUrlResponse
import api.routes.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post

/**
 * Client for authentication-related API calls.
 */
class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    /**
     * Get the Google OAuth login URL.
     */
    suspend fun getGoogleLoginUrl(): LoginUrlResponse {
        return httpClient.get("$baseUrl${ApiRoutes.AUTH_GOOGLE_LOGIN}").body()
    }

    /**
     * Logout and invalidate session.
     */
    suspend fun logout(sessionToken: String) {
        httpClient.post("$baseUrl${ApiRoutes.AUTH_LOGOUT}") {
            bearerAuth(sessionToken)
        }
    }
}
