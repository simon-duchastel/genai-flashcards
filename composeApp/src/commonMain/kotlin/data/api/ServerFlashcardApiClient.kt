package data.api

import api.routes.ApiRoutes
import domain.model.FlashcardSet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/**
 * Client for interacting with server flashcard API endpoints.
 * Handles HTTP communication for flashcard CRUD operations.
 */
class ServerFlashcardApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    /**
     * Get all flashcard sets for the authenticated user.
     * @param token Bearer token for authentication
     * @return List of flashcard sets, or null if request fails
     */
    suspend fun getAllFlashcardSets(token: String): List<FlashcardSet>? {
        return try {
            val response = httpClient.get("$baseUrl${ApiRoutes.FLASHCARD_SETS}") {
                bearerAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                response.body<List<FlashcardSet>>()
            } else {
                println("Failed to fetch flashcard sets: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error fetching flashcard sets: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Save a flashcard set to the server.
     * @param token Bearer token for authentication
     * @param set FlashcardSet to save
     * @return Saved FlashcardSet with server-assigned metadata, or null if request fails
     */
    suspend fun saveFlashcardSet(token: String, set: FlashcardSet): FlashcardSet? {
        return try {
            val response = httpClient.post("$baseUrl${ApiRoutes.FLASHCARD_SETS}") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(set)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<FlashcardSet>()
            } else {
                println("Failed to save flashcard set: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error saving flashcard set: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete a flashcard set from the server.
     * @param token Bearer token for authentication
     * @param id ID of the flashcard set to delete
     * @return true if deletion succeeded, false otherwise
     */
    suspend fun deleteFlashcardSet(token: String, id: String): Boolean {
        return try {
            val response = httpClient.delete("$baseUrl${ApiRoutes.flashcardSet(id)}") {
                bearerAuth(token)
            }

            response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error deleting flashcard set: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Get a specific flashcard set by ID.
     * @param token Bearer token for authentication
     * @param id ID of the flashcard set to retrieve
     * @return FlashcardSet if found, null otherwise
     */
    suspend fun getFlashcardSet(token: String, id: String): FlashcardSet? {
        return try {
            val response = httpClient.get("$baseUrl${ApiRoutes.flashcardSet(id)}") {
                bearerAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                response.body<FlashcardSet>()
            } else {
                println("Failed to fetch flashcard set $id: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error fetching flashcard set $id: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
