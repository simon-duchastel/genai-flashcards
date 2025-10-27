package com.flashcards.server

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
    val googleWebRedirectUri = System.getenv("GOOGLE_OAUTH_WEB_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_WEB_REDIRECT_URI environment variable not set")
    val googleIosRedirectUri = System.getenv("GOOGLE_OAUTH_IOS_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_IOS_REDIRECT_URI environment variable not set")

    val googleOAuthService = GoogleOAuthService(
        clientId = googleClientId,
        clientSecret = googleClientSecret,
        webRedirectUri = googleWebRedirectUri,
        iosRedirectUri = googleIosRedirectUri
    )

    val testGoogleClientId = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_ID")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_ID environment variable not set")
    val testGoogleClientSecret = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_SECRET")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_SECRET environment variable not set")
    val testGoogleWebRedirectUri = System.getenv("GOOGLE_OAUTH_TEST_WEB_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_TEST_WEB_REDIRECT_URI environment variable not set")
    val testGoogleIosRedirectUri = System.getenv("GOOGLE_OAUTH_TEST_IOS_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_TEST_IOS_REDIRECT_URI environment variable not set")

    val testGoogleOAuthService = GoogleOAuthService(
        clientId = testGoogleClientId,
        clientSecret = testGoogleClientSecret,
        webRedirectUri = testGoogleWebRedirectUri,
        iosRedirectUri = testGoogleIosRedirectUri
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
        testGoogleOAuthService = testGoogleOAuthService
    )
}
