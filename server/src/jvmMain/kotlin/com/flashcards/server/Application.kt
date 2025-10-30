package com.flashcards.server

import com.flashcards.server.auth.AppleOAuthService
import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.config.FirestoreConfig
import com.flashcards.server.plugins.*
import com.flashcards.server.repository.FirestoreAuthRepository
import com.flashcards.server.repository.ServerFlashcardRepository
import com.flashcards.server.storage.FirestoreRateLimiter
import com.flashcards.server.storage.FirestoreStorage
import domain.generator.KoogFlashcardGenerator
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * OAuth redirect URI configuration.
 * These are the URIs that OAuth providers (Google, Apple) redirect back to after authentication.
 */
object OAuthConfig {
    private const val API_BASE = "https://api.flashcards.solenne.ai/api/v1/auth"

    // Google OAuth redirect URIs
    const val GOOGLE_WEB_REDIRECT_URI = "$API_BASE/google/callback"
    const val GOOGLE_IOS_REDIRECT_URI = "$API_BASE/google/callback/ios"
    const val GOOGLE_ANDROID_REDIRECT_URI = "$API_BASE/google/callback/android"
    const val GOOGLE_TEST_WEB_REDIRECT_URI = "$API_BASE/google/test/callback"
    const val GOOGLE_TEST_IOS_REDIRECT_URI = "$API_BASE/google/test/callback"
    const val GOOGLE_TEST_ANDROID_REDIRECT_URI = "$API_BASE/google/test/callback"

    // Apple OAuth redirect URIs
    const val APPLE_WEB_REDIRECT_URI = "$API_BASE/apple/callback"
    const val APPLE_IOS_REDIRECT_URI = "$API_BASE/apple/callback/ios"
    const val APPLE_ANDROID_REDIRECT_URI = "$API_BASE/apple/callback/android"
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(
        Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val firestore = FirestoreConfig.initialize()

    // Create Firestore-backed repositories and storage
    val storage = FirestoreStorage(firestore)
    val repository = ServerFlashcardRepository(storage)
    val authRepository = FirestoreAuthRepository(firestore)
    val rateLimiter = FirestoreRateLimiter(firestore)

    val googleClientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID")
        ?: error("GOOGLE_OAUTH_CLIENT_ID environment variable not set")
    val googleClientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET")
        ?: error("GOOGLE_OAUTH_CLIENT_SECRET environment variable not set")

    val googleOAuthService = GoogleOAuthService(
        clientId = googleClientId,
        clientSecret = googleClientSecret,
        webRedirectUri = OAuthConfig.GOOGLE_WEB_REDIRECT_URI,
        iosRedirectUri = OAuthConfig.GOOGLE_IOS_REDIRECT_URI,
        androidRedirectUri = OAuthConfig.GOOGLE_ANDROID_REDIRECT_URI,
    )

    val testGoogleClientId = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_ID")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_ID environment variable not set")
    val testGoogleClientSecret = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_SECRET")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_SECRET environment variable not set")

    val testGoogleOAuthService = GoogleOAuthService(
        clientId = testGoogleClientId,
        clientSecret = testGoogleClientSecret,
        webRedirectUri = OAuthConfig.GOOGLE_TEST_WEB_REDIRECT_URI,
        iosRedirectUri = OAuthConfig.GOOGLE_TEST_IOS_REDIRECT_URI,
        androidRedirectUri = OAuthConfig.GOOGLE_TEST_ANDROID_REDIRECT_URI,
    )

    val appleTeamId = System.getenv("APPLE_TEAM_ID")
        ?: error("APPLE_TEAM_ID environment variable not set")
    val appleServiceId = System.getenv("APPLE_SERVICE_ID")
        ?: error("APPLE_SERVICE_ID environment variable not set")
    val appleKeyId = System.getenv("APPLE_KEY_ID")
        ?: error("APPLE_KEY_ID environment variable not set")
    val applePrivateKeyPem = System.getenv("APPLE_PRIVATE_KEY_PEM")
        ?: error("APPLE_PRIVATE_KEY_PEM environment variable not set")

    val appleOAuthService = AppleOAuthService(
        teamId = appleTeamId,
        serviceId = appleServiceId,
        keyId = appleKeyId,
        privateKeyPem = applePrivateKeyPem,
        webRedirectUri = OAuthConfig.APPLE_WEB_REDIRECT_URI,
        iosRedirectUri = OAuthConfig.APPLE_IOS_REDIRECT_URI,
        androidRedirectUri = OAuthConfig.APPLE_ANDROID_REDIRECT_URI,
    )

    val geminiApiKey: String = System.getenv("GEMINI_API_KEY")
        ?: error("GEMINI_API_KEY environment variable not set")
    val generator = KoogFlashcardGenerator(getGeminiApiKey = { geminiApiKey })

    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication(authRepository)
    configureRouting(
        repository = repository,
        generator = generator,
        rateLimiter = rateLimiter,
        authRepository = authRepository,
        googleOAuthService = googleOAuthService,
        testGoogleOAuthService = testGoogleOAuthService,
        appleOAuthService = appleOAuthService,
        storage = storage
    )
}
