package ai.solenne.flashcards.server.plugins

import ai.solenne.flashcards.server.auth.AppleOAuthService
import ai.solenne.flashcards.server.auth.GoogleOAuthService
import ai.solenne.flashcards.server.repository.AuthRepository
import ai.solenne.flashcards.server.repository.ServerFlashcardRepository
import ai.solenne.flashcards.server.routes.authRoutes
import ai.solenne.flashcards.server.routes.flashcardRoutes
import ai.solenne.flashcards.server.routes.generatorRoutes
import ai.solenne.flashcards.server.storage.RateLimiter
import ai.solenne.flashcards.shared.domain.generator.KoogFlashcardGenerator
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    repository: ServerFlashcardRepository,
    generator: KoogFlashcardGenerator,
    rateLimiter: RateLimiter,
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService,
    testGoogleOAuthService: GoogleOAuthService,
    appleOAuthService: AppleOAuthService,
    storage: ai.solenne.flashcards.server.storage.FirestoreStorage
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
            testGoogleOAuthService = testGoogleOAuthService,
            appleOAuthService = appleOAuthService,
            storage = storage
        )

        // Protected API routes
        flashcardRoutes(repository)
        generatorRoutes(generator, rateLimiter)
    }
}
