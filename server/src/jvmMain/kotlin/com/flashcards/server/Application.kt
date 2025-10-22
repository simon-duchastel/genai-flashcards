package com.flashcards.server

import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.plugins.*
import com.flashcards.server.repository.InMemoryAuthRepository
import com.flashcards.server.repository.ServerFlashcardRepository
import com.flashcards.server.storage.InMemoryStorage
import domain.generator.KoogFlashcardGenerator
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val storage = InMemoryStorage()
    val repository = ServerFlashcardRepository(storage)
    val authRepository = InMemoryAuthRepository()

    val googleClientId = System.getenv("GOOGLE_CLIENT_ID") ?: "test"
//        ?: error("GOOGLE_CLIENT_ID environment variable not set")
    val googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: "test"
//        ?: error("GOOGLE_CLIENT_SECRET environment variable not set")
    val googleRedirectUri = System.getenv("GOOGLE_REDIRECT_URI") ?: "test"
//        ?: error("GOOGLE_REDIRECT_URI environment variable not set")

    val googleOAuthService = GoogleOAuthService(
        clientId = googleClientId,
        clientSecret = googleClientSecret,
        redirectUri = googleRedirectUri
    )

    val geminiApiKey: String = System.getenv("GEMINI_API_KEY") ?: "test"
    val generator = KoogFlashcardGenerator(getGeminiApiKey = { geminiApiKey })

    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication(authRepository)
    configureRouting(repository, generator, authRepository, googleOAuthService)
}
