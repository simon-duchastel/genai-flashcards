package data.api

import api.dto.LoginUrlResponse
import api.dto.MeResponse
import api.dto.OAuthPlatform
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
     *
     * @param platform The platform type for OAuth redirect
     */
    suspend fun startGoogleLogin(platform: OAuthPlatform): LoginUrlResponse {
        val route = if (isTest) {
            ApiRoutes.AUTH_GOOGLE_LOGIN_TEST
        } else {
            ApiRoutes.AUTH_GOOGLE_LOGIN
        }
        val url = when (platform) {
            OAuthPlatform.WEB -> "$baseUrl$route"
            OAuthPlatform.IOS -> "$baseUrl$route?platform=ios"
        }
        return httpClient.get(url).body()
    }

    /**
     * Get the Apple OAuth login URL.
     *
     * @param platform The platform type for OAuth redirect
     */
    suspend fun startAppleLogin(platform: OAuthPlatform): LoginUrlResponse {
        val url = when (platform) {
            OAuthPlatform.WEB -> "$baseUrl${ApiRoutes.AUTH_APPLE_LOGIN}"
            OAuthPlatform.IOS -> "$baseUrl${ApiRoutes.AUTH_APPLE_LOGIN}?platform=ios"
        }
        return httpClient.get(url).body()
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
