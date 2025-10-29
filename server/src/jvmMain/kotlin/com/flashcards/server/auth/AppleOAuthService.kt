package com.flashcards.server.auth

import api.dto.OAuthPlatform
import com.flashcards.server.plugins.jsonParser
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
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
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

/**
 * Service for handling Apple OAuth 2.0 authentication flow.
 */
class AppleOAuthService(
    private val teamId: String,
    private val serviceId: String,
    private val keyId: String,
    private val privateKeyPem: String,
    private val webRedirectUri: String,
    private val iosRedirectUri: String,
) {
    private val httpClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    private val privateKey: ECPrivateKey by lazy {
        loadPrivateKey(privateKeyPem)
    }

    companion object {
        private const val APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize"
        private const val APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token"
        private const val SCOPES = "name"
        private const val CLIENT_SECRET_EXPIRY_MINUTES = 5L
    }

    /**
     * Generate the Apple OAuth authorization URL.
     *
     * @param platform The platform type to get the appropriate redirect URI
     */
    fun getAuthorizationUrl(platform: OAuthPlatform = OAuthPlatform.WEB): String {
        val redirectUri = when (platform) {
            OAuthPlatform.WEB -> webRedirectUri
            OAuthPlatform.IOS -> iosRedirectUri
        }

        return URLBuilder(APPLE_AUTH_URL).apply {
            parameters.append("client_id", serviceId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", SCOPES)
            parameters.append("response_mode", "query")
        }.buildString()
    }

    /**
     * Exchange authorization code for tokens and user info.
     *
     * @param code The authorization code from Apple
     * @param platform The platform type that was used in the authorization request
     * @return User information extracted from the ID token
     * @throws Exception if token exchange or validation fails
     */
    suspend fun exchangeCodeForUser(code: String, platform: OAuthPlatform = OAuthPlatform.WEB): User {
        val redirectUri = when (platform) {
            OAuthPlatform.WEB -> webRedirectUri
            OAuthPlatform.IOS -> iosRedirectUri
        }

        // Generate client secret JWT
        val clientSecret = generateClientSecret()

        // Exchange code for tokens
        val tokenResponse = httpClient.submitForm(
            url = APPLE_TOKEN_URL,
            formParameters = parameters {
                append("code", code)
                append("client_id", serviceId)
                append("client_secret", clientSecret)
                append("redirect_uri", redirectUri)
                append("grant_type", "authorization_code")
            }
        ).body<AppleTokenResponse>()

        // Parse and validate ID token
        val userInfo = parseIdToken(tokenResponse.idToken)

        return User(
            authId = "apple-${userInfo.sub}",
        )
    }

    /**
     * Generate a client secret JWT for Apple OAuth.
     *
     * Apple requires a JWT signed with ES256 algorithm using your private key.
     * The JWT is used as the client_secret in token exchange requests.
     *
     * JWT Structure:
     * Header: { "alg": "ES256", "kid": keyId }
     * Payload: {
     *   "iss": teamId,
     *   "iat": <current time>,
     *   "exp": <current time + 5 minutes>,
     *   "aud": "https://appleid.apple.com",
     *   "sub": serviceId
     * }
     */
    private fun generateClientSecret(): String {
        val now = Date()
        val expirationTime = Date(now.time + CLIENT_SECRET_EXPIRY_MINUTES * 60 * 1000)

        val claimsSet = JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(now)
            .expirationTime(expirationTime)
            .audience("https://appleid.apple.com")
            .subject(serviceId)
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(keyId)
            .build()

        val signedJWT = SignedJWT(header, claimsSet)
        val signer = ECDSASigner(privateKey)
        signedJWT.sign(signer)

        return signedJWT.serialize()
    }

    /**
     * Parse and validate Apple ID token (JWT).
     *
     * For production, should verify signature with Apple's public keys.
     * For MVP, we trust the token since it comes from Apple's token endpoint over HTTPS.
     */
    private fun parseIdToken(idToken: String): AppleIdTokenPayload {
        val parts = idToken.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        // Decode payload (second part of JWT)
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        return jsonParser.decodeFromString<AppleIdTokenPayload>(payload)
    }

    /**
     * Load EC private key from PEM string.
     *
     * Apple requires ES256 (ECDSA with P-256 and SHA-256) private key.
     * The key should be in PKCS#8 format.
     */
    private fun loadPrivateKey(pemKey: String): ECPrivateKey {
        // Remove PEM header/footer and whitespace
        val privateKeyContent = pemKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        // Decode base64
        val keyBytes = Base64.getDecoder().decode(privateKeyContent)

        // Parse as PKCS#8
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(spec) as ECPrivateKey
    }

    /**
     * Response from Apple token endpoint.
     */
    @Serializable
    private data class AppleTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Int,
        @SerialName("token_type") val tokenType: String,
        @SerialName("id_token") val idToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null
    )

    /**
     * Payload from Apple ID token (JWT).
     */
    @Serializable
    private data class AppleIdTokenPayload(
        val sub: String,             // Apple user ID (unique per service ID)
        val iss: String,             // Issuer (https://appleid.apple.com)
        val aud: String,             // Audience (your service ID)
        val exp: Long,               // Expiration
        val iat: Long,               // Issued at
        @SerialName("auth_time") val authTime: Long? = null
    )
}
