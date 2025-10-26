package com.flashcards.server.plugins

import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.repository.AuthRepository
import com.flashcards.server.repository.ServerFlashcardRepository
import com.flashcards.server.routes.authRoutes
import com.flashcards.server.routes.flashcardRoutes
import com.flashcards.server.routes.generatorRoutes
import domain.generator.KoogFlashcardGenerator
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    repository: ServerFlashcardRepository,
    generator: KoogFlashcardGenerator,
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService,
    testGoogleOAuthService: GoogleOAuthService
) {
    routing {
        // Health check endpoint (public)
        get("/health") {
            call.respondText("OK")
        }

        // Authentication routes
        authRoutes(
            authRepository = authRepository,
            googleOAuthService = googleOAuthService,
            testGoogleOAuthService = testGoogleOAuthService
        )

        // Protected API routes
        flashcardRoutes(repository)
        generatorRoutes(generator)
    }
}
