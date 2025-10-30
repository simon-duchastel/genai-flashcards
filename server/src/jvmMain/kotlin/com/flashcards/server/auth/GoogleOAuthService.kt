package com.flashcards.server.auth

import api.dto.OAuthPlatform
import com.flashcards.server.plugins.jsonParser
import domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.URLBuilder
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Base64

/**
 * Service for handling Google OAuth 2.0 authentication flow.
 */
class GoogleOAuthService(
    private val clientId: String,
    private val clientSecret: String,
    private val webRedirectUri: String,
    private val iosRedirectUri: String,
    private val androidRedirectUri: String,
) {
    private val httpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    companion object {
        private const val GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val SCOPES = "openid"
    }

    /**
     * Generate the Google OAuth authorization URL.
     *
     * @param platform The platform type to get the appropriate redirect URI
     */
    fun getAuthorizationUrl(platform: OAuthPlatform): String {
        val redirectUri = when (platform) {
            OAuthPlatform.WEB -> webRedirectUri
            OAuthPlatform.IOS -> iosRedirectUri
            OAuthPlatform.ANDROID -> androidRedirectUri
        }

        return URLBuilder(GOOGLE_AUTH_URL).apply {
            parameters.append("client_id", clientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", SCOPES)
            parameters.append("prompt", "consent")
        }.buildString()
    }

    /**
     * Exchange authorization code for tokens and user info.
     *
     * @param code The authorization code from Google
     * @param platform The platform type that was used in the authorization request
     * @return User information extracted from the ID token
     * @throws Exception if token exchange or validation fails
     */
    suspend fun exchangeCodeForUser(code: String, platform: OAuthPlatform): User {
        val redirectUri = when (platform) {
            OAuthPlatform.WEB -> webRedirectUri
            OAuthPlatform.IOS -> iosRedirectUri
            OAuthPlatform.ANDROID -> androidRedirectUri
        }

        // Exchange code for tokens
        val tokenResponse = httpClient.submitForm(
            url = GOOGLE_TOKEN_URL,
            formParameters = parameters {
                append("code", code)
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("redirect_uri", redirectUri)
                append("grant_type", "authorization_code")
            }
        ).body<GoogleTokenResponse>()

        // Parse and validate ID token
        val userInfo = parseIdToken(tokenResponse.idToken)

        return User(
            authId = "google-${userInfo.sub}",
        )
    }

    /**
     * Parse and validate Google ID token (JWT).
     * For production, should verify signature with Google's public keys.
     * For MVP, we trust the token since it comes from Google's token endpoint over HTTPS.
     */
    private fun parseIdToken(idToken: String): GoogleIdTokenPayload {
        val parts = idToken.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        // Decode payload (second part of JWT)
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        return jsonParser.decodeFromString<GoogleIdTokenPayload>(payload)
    }

    /**
     * Response from Google token endpoint.
     */
    @Serializable
    private data class GoogleTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Int,
        @SerialName("token_type") val tokenType: String,
        @SerialName("scope") val scope: String,
        @SerialName("id_token") val idToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null
    )

    /**
     * Payload from Google ID token (JWT).
     */
    @Serializable
    private data class GoogleIdTokenPayload(
        val sub: String,  // Google user ID
        val iss: String,  // Issuer
        val aud: String,  // Audience
        val exp: Long,    // Expiration
        val iat: Long     // Issued at
    )
}
