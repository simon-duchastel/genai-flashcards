package ai.solenne.flashcards.server.routes

import ai.solenne.flashcards.shared.api.dto.ErrorResponse
import ai.solenne.flashcards.shared.api.routes.ApiRoutes
import ai.solenne.flashcards.server.auth.AuthenticatedUser
import ai.solenne.flashcards.server.repository.ServerFlashcardRepository
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Configure flashcard CRUD routes (all protected by authentication).
 */
fun Route.flashcardRoutes(repository: ServerFlashcardRepository) {
    authenticate("auth-bearer") {
        route(ApiRoutes.FLASHCARD_SETS) {
            // GET /api/v1/flashcards/sets - Get all flashcard sets for authenticated user
            get {
                val principal = call.principal<AuthenticatedUser>()
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Authentication required", "UNAUTHORIZED")
                    )

                val sets = repository.getAllFlashcardSets(principal.userId)
                call.respond(HttpStatusCode.OK, sets)
            }

            // POST /api/v1/flashcards/sets - Create/save a flashcard set
            post {
                val principal = call.principal<AuthenticatedUser>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Authentication required", "UNAUTHORIZED")
                    )

                val set = call.receive<FlashcardSet>()
                // Ensure userId matches authenticated user (prevent tampering)
                val userSet = set.copy(userId = principal.userId)
                repository.saveFlashcardSet(userSet)
                call.respond(HttpStatusCode.Created, userSet)
            }

            // PUT /api/v1/flashcards/sets - Update a flashcard set (idempotent)
            put {
                val principal = call.principal<AuthenticatedUser>()
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Authentication required", "UNAUTHORIZED")
                    )

                val set = call.receive<FlashcardSet>()

                // Verify the set exists and belongs to the user
                val existingSet = repository.getFlashcardSet(set.id, principal.userId)
                if (existingSet == null) {
                    return@put call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Flashcard set not found", "NOT_FOUND")
                    )
                }

                // Ensure userId matches authenticated user (prevent tampering)
                val userSet = set.copy(userId = principal.userId)
                repository.saveFlashcardSet(userSet)
                call.respond(HttpStatusCode.OK, userSet)
            }
        }

        // GET /api/v1/flashcards/sets/{id} - Get specific flashcard set
        get(ApiRoutes.flashcardSet("{id}")) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
                )

            val set = repository.getFlashcardSet(id, principal.userId)
            if (set == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Flashcard set not found", "NOT_FOUND")
                )
            } else {
                call.respond(HttpStatusCode.OK, set)
            }
        }

        // DELETE /api/v1/flashcards/sets/{id} - Delete flashcard set
        delete(ApiRoutes.flashcardSet("{id}")) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val id = call.parameters["id"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
                )

            repository.deleteFlashcardSet(id, principal.userId)
            call.respond(HttpStatusCode.NoContent)
        }

        // GET /api/v1/flashcards/sets/{id}/randomized - Get randomized flashcards
        get(ApiRoutes.randomizedFlashcards("{id}")) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
                )

            val flashcards = repository.getRandomizedFlashcards(id, principal.userId)
            if (flashcards == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("Flashcard set not found", "NOT_FOUND")
                )
            } else {
                call.respond(HttpStatusCode.OK, flashcards)
            }
        }
    }
}
