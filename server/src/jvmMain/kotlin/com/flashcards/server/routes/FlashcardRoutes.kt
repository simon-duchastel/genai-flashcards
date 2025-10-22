package com.flashcards.server.routes

import api.dto.ErrorResponse
import api.routes.ApiRoutes
import com.flashcards.server.repository.ServerFlashcardRepository
import domain.model.FlashcardSet
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Configure flashcard CRUD routes.
 */
fun Route.flashcardRoutes(repository: ServerFlashcardRepository) {
    route(ApiRoutes.FLASHCARD_SETS) {
        // GET /api/v1/flashcards/sets - Get all flashcard sets
        get {
            val sets = repository.getAllFlashcardSets()
            call.respond(HttpStatusCode.OK, sets)
        }

        // POST /api/v1/flashcards/sets - Create/save a flashcard set
        post {
            val set = call.receive<FlashcardSet>()
            repository.saveFlashcardSet(set)
            call.respond(HttpStatusCode.Created, set)
        }
    }

    // GET /api/v1/flashcards/sets/{id} - Get specific flashcard set
    get(ApiRoutes.flashcardSet("{id}")) {
        val id = call.parameters["id"]
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
            )

        val set = repository.getFlashcardSet(id)
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
        val id = call.parameters["id"]
            ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
            )

        repository.deleteFlashcardSet(id)
        call.respond(HttpStatusCode.NoContent)
    }

    // GET /api/v1/flashcards/sets/{id}/randomized - Get randomized flashcards
    get(ApiRoutes.randomizedFlashcards("{id}")) {
        val id = call.parameters["id"]
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing id parameter", "MISSING_PARAMETER")
            )

        val flashcards = repository.getRandomizedFlashcards(id)
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
