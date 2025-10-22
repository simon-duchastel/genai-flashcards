package api.routes

/**
 * API route constants shared between client and server.
 */
object ApiRoutes {
    private const val API_BASE = "/api/v1"
    const val FLASHCARD_SETS = "$API_BASE/flashcards/sets"
    fun flashcardSet(id: String) = "$FLASHCARD_SETS/$id"
    fun randomizedFlashcards(id: String) = "$FLASHCARD_SETS/$id/randomized"

    const val GENERATE = "$API_BASE/generate"
}
