package com.flashcards.server.routes

import api.dto.ErrorResponse
import api.dto.GenerateRequest
import api.dto.GenerateResponse
import api.routes.ApiRoutes
import com.flashcards.server.auth.AuthenticatedUser
import domain.generator.KoogFlashcardGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

/**
 * Configure flashcard generation routes (protected by authentication).
 */
fun Route.generatorRoutes(generator: KoogFlashcardGenerator) {
    authenticate("auth-bearer") {
        // POST /api/v1/generate - Generate flashcards
        post(ApiRoutes.GENERATE) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val request = call.receive<GenerateRequest>()

            // Validate request
            if (request.topic.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    GenerateResponse(error = "Topic cannot be empty")
                )
            }

            if (request.count <= 0 || request.count > 100) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    GenerateResponse(error = "Count must be between 1 and 100")
                )
            }

            // Generate flashcards
            val flashcardSet = generator.generate(
                topic = request.topic,
                count = request.count,
                userQuery = request.userQuery,
            )

            if (flashcardSet == null) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    GenerateResponse(error = "Failed to generate flashcards. Please check your API key.")
                )
            } else {
                // Set userId on generated flashcard set
                val userFlashcardSet = flashcardSet.copy(userId = principal.userId)
                call.respond(
                    HttpStatusCode.OK,
                    GenerateResponse(flashcardSet = userFlashcardSet)
                )
            }
        }
    }
}
