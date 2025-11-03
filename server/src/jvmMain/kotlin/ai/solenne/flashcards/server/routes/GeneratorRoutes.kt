package ai.solenne.flashcards.server.routes

import ai.solenne.flashcards.shared.api.dto.ErrorResponse
import ai.solenne.flashcards.shared.api.dto.GenerateRequest
import ai.solenne.flashcards.shared.api.dto.GenerateResponse
import ai.solenne.flashcards.shared.api.dto.RateLimitError
import ai.solenne.flashcards.shared.api.dto.RegenerateRequest
import ai.solenne.flashcards.shared.api.routes.ApiRoutes
import ai.solenne.flashcards.server.auth.AuthenticatedUser
import ai.solenne.flashcards.server.storage.RateLimiter
import ai.solenne.flashcards.server.storage.RateLimitResult
import ai.solenne.flashcards.shared.domain.generator.KoogFlashcardGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

/**
 * Configure flashcard generation routes (protected by authentication).
 */
fun Route.generatorRoutes(generator: KoogFlashcardGenerator, rateLimiter: RateLimiter) {
    authenticate("auth-bearer") {
        // POST /api/v1/generate - Generate flashcards
        post(ApiRoutes.GENERATE) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            when (val result = rateLimiter.checkRateLimit(principal.userId)) {
                is RateLimitResult.RateLimitExceeded -> {
                    return@post call.respond(
                        HttpStatusCode.TooManyRequests,
                        RateLimitError(
                            message = "You've exceeded your number of flashcard generations for today",
                            tryAgainAt = result.tryAgainAt,
                            numberOfGenerations = result.numberOfGenerations
                        )
                    )
                }
                RateLimitResult.Ok -> Unit // do nothing - continue with generation
            }

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
            )?.copy(userId = principal.userId)

            if (flashcardSet == null) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    GenerateResponse(error = "Failed to generate flashcards. Please check your API key.")
                )
            } else {
                rateLimiter.recordAttempt(principal.userId)

                call.respond(
                    HttpStatusCode.OK,
                    GenerateResponse(flashcardSet = flashcardSet)
                )
            }
        }

        // POST /api/v1/regenerate - Regenerate flashcards
        post(ApiRoutes.REGENERATE) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            when (val result = rateLimiter.checkRateLimit(principal.userId)) {
                is RateLimitResult.RateLimitExceeded -> {
                    return@post call.respond(
                        HttpStatusCode.TooManyRequests,
                        RateLimitError(
                            message = "You've exceeded your number of flashcard generations for today",
                            tryAgainAt = result.tryAgainAt,
                            numberOfGenerations = result.numberOfGenerations
                        )
                    )
                }
                RateLimitResult.Ok -> Unit // do nothing - continue with generation
            }

            val request = call.receive<RegenerateRequest>()

            // Validate request
            if (request.flashcardSet.flashcards.isEmpty()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    GenerateResponse(error = "Must provide existing flashcards to regenerate")
                )
            }

            // Regenerate flashcards
            val flashcardSet = generator.regenerate(
                existingSet = request.flashcardSet,
                regenerationPrompt = request.regenerationPrompt,
            )?.copy(userId = principal.userId)

            if (flashcardSet == null) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    GenerateResponse(error = "Failed to regenerate flashcards. Please check your API key.")
                )
            } else {
                rateLimiter.recordAttempt(principal.userId)

                call.respond(
                    HttpStatusCode.OK,
                    GenerateResponse(flashcardSet = flashcardSet)
                )
            }
        }
    }
}
