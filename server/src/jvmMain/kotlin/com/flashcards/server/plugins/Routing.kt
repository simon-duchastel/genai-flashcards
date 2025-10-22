package com.flashcards.server.plugins

import com.flashcards.server.repository.ServerFlashcardRepository
import com.flashcards.server.routes.flashcardRoutes
import com.flashcards.server.routes.generatorRoutes
import domain.generator.KoogFlashcardGenerator
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    repository: ServerFlashcardRepository,
    generator: KoogFlashcardGenerator
) {
    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }

        // API routes
        flashcardRoutes(repository)
        generatorRoutes(generator)
    }
}
