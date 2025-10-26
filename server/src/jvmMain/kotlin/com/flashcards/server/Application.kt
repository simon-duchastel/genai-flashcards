package com.flashcards.server

import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.plugins.*
import com.flashcards.server.repository.InMemoryAuthRepository
import com.flashcards.server.repository.ServerFlashcardRepository
import com.flashcards.server.storage.GenerationRateLimiter
import com.flashcards.server.storage.InMemoryStorage
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
    val storage = InMemoryStorage()
    val repository = ServerFlashcardRepository(storage)
    val authRepository = InMemoryAuthRepository()
    val rateLimiter = GenerationRateLimiter()

    val googleClientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID")
        ?: error("GOOGLE_OAUTH_CLIENT_ID environment variable not set")
    val googleClientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET")
        ?: error("GOOGLE_OAUTH_CLIENT_SECRET environment variable not set")
    val googleRedirectUri = System.getenv("GOOGLE_OAUTH_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_REDIRECT_URI environment variable not set")

    val googleOAuthService = GoogleOAuthService(
        clientId = googleClientId,
        clientSecret = googleClientSecret,
        redirectUri = googleRedirectUri
    )

    val testGoogleClientId = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_ID")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_ID environment variable not set")
    val testGoogleClientSecret = System.getenv("GOOGLE_OAUTH_TEST_CLIENT_SECRET")
        ?: error("GOOGLE_OAUTH_TEST_CLIENT_SECRET environment variable not set")
    val testGoogleRedirectUri = System.getenv("GOOGLE_OAUTH_TEST_REDIRECT_URI")
        ?: error("GOOGLE_OAUTH_TEST_REDIRECT_URI environment variable not set")

    val testGoogleOAuthService = GoogleOAuthService(
        clientId = testGoogleClientId,
        clientSecret = testGoogleClientSecret,
        redirectUri = testGoogleRedirectUri
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
