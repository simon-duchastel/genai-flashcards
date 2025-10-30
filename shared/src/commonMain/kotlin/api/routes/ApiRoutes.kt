package api.routes

/**
 * API route constants shared between client and server.
 */
object ApiRoutes {
    private const val API_BASE = "/api/v1"

    // Authentication routes
    private const val AUTH_BASE = "$API_BASE/auth"

    const val WEB_CLIENT = "https://flashcards.solenne.ai"
    const val TEST_WEB_CLIENT = "http://localhost:8080"
    const val IOS_CLIENT = "solenne-flashcards://callback"
    const val AUTH_GOOGLE_LOGIN = "$AUTH_BASE/google/login"
    const val AUTH_GOOGLE_CALLBACK = "$AUTH_BASE/google/callback"
    const val AUTH_GOOGLE_CALLBACK_IOS = "$AUTH_BASE/google/callback/ios"
    const val AUTH_GOOGLE_LOGIN_TEST = "$AUTH_BASE/google/test/login"
    const val AUTH_GOOGLE_CALLBACK_TEST = "$AUTH_BASE/google/test/callback"
    const val AUTH_APPLE_LOGIN = "$AUTH_BASE/apple/login"
    const val AUTH_APPLE_CALLBACK = "$AUTH_BASE/apple/callback"
    const val AUTH_APPLE_CALLBACK_IOS = "$AUTH_BASE/apple/callback/ios"
    const val AUTH_LOGOUT = "$AUTH_BASE/logout"
    const val AUTH_ME = "$AUTH_BASE/me"

    // Flashcard routes
    const val FLASHCARD_SETS = "$API_BASE/flashcards/sets"
    fun flashcardSet(id: String) = "$FLASHCARD_SETS/$id"
    fun randomizedFlashcards(id: String) = "$FLASHCARD_SETS/$id/randomized"

    // Generator routes
    const val GENERATE = "$API_BASE/generate"
}
