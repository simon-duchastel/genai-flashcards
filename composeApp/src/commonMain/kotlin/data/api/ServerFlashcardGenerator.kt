package data.api

import api.dto.GenerateRequest
import api.dto.GenerateResponse
import api.dto.RateLimitError
import api.routes.ApiRoutes
import data.storage.ConfigRepository
import domain.model.FlashcardSet
import domain.repository.FlashcardGenerator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/**
 * Server-side implementation of FlashcardGenerator.
 * Calls the server's /api/v1/generate endpoint.
 */
class ServerFlashcardGenerator(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val configRepository: ConfigRepository
) : FlashcardGenerator {

    /**
     * Exception thrown when rate limit is exceeded.
     */
    class RateLimitException(
        val tryAgainAt: Long,
        val numberOfGenerations: Int,
        message: String
    ) : Exception(message)

    override suspend fun generate(topic: String, count: Int, userQuery: String): FlashcardSet? {
        return try {
            val token = configRepository.getSessionToken()
                ?: throw IllegalStateException("Not authenticated")

            val response: HttpResponse = httpClient.post("$baseUrl${ApiRoutes.GENERATE}") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(GenerateRequest(
                    topic = topic,
                    count = count,
                    userQuery = userQuery,
                ))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val generateResponse: GenerateResponse = response.body()
                    generateResponse.flashcardSet
                }
                HttpStatusCode.TooManyRequests -> {
                    val rateLimitError: RateLimitError = response.body()
                    throw RateLimitException(
                        tryAgainAt = rateLimitError.tryAgainAt,
                        numberOfGenerations = rateLimitError.numberOfGenerations,
                        message = rateLimitError.message
                    )
                }
                else -> {
                    val errorResponse: GenerateResponse = response.body()
                    throw Exception(errorResponse.error ?: "Failed to generate flashcards")
                }
            }
        } catch (e: RateLimitException) {
            // Re-throw rate limit exceptions so they can be handled by the UI
            throw e
        } catch (e: Exception) {
            println("Failed to generate flashcards: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
