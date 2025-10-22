package com.flashcards.server

import com.flashcards.server.plugins.*
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

    val geminiApiKey = System.getenv("GEMINI_API_KEY")
    val generator = KoogFlashcardGenerator(getGeminiApiKey =  { geminiApiKey })

    configureSerialization()
    configureCORS()
    configureCallLogging()
    configureStatusPages()
    configureRouting(repository, generator)
}
