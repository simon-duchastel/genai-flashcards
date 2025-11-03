package ai.solenne.flashcards.app.data.storage

/**
 * Platform-specific storage for configuration values.
 */
interface ConfigRepository {
    // Gemini API Key
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(apiKey: String)

    // Theme
    suspend fun isDarkMode(): Boolean
    suspend fun setDarkMode(isDark: Boolean)

    // Session Management
    suspend fun getSessionToken(): String?
    suspend fun setSessionToken(token: String)
    suspend fun clearSessionToken()
}

expect fun getConfigRepository(): ConfigRepository
