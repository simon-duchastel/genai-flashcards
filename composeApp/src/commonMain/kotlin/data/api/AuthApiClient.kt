package data.api

import api.dto.LoginUrlResponse
import api.dto.MeResponse
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
    private val isTest: Boolean,
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    /**
     * Get the Google OAuth login URL.
     */
    suspend fun startGoogleLogin(): LoginUrlResponse {
        val route = if (isTest) {
            ApiRoutes.AUTH_GOOGLE_LOGIN_TEST
        } else {
            ApiRoutes.AUTH_GOOGLE_LOGIN
        }
        return httpClient.get("$baseUrl$route").body()
    }

    /**
     * Get current user info.
     */
    suspend fun getMe(sessionToken: String): MeResponse {
        return httpClient.get("$baseUrl${ApiRoutes.AUTH_ME}") {
            bearerAuth(sessionToken)
        }.body()
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
