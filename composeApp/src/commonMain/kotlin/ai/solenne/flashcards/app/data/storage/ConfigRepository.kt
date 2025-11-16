package ai.solenne.flashcards.app.data.storage

import ai.solenne.flashcards.shared.api.dto.OAuthProvider

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

    // OAuth Provider
    suspend fun getOAuthProvider(): OAuthProvider?
    suspend fun setOAuthProvider(provider: OAuthProvider)
}

expect fun getConfigRepository(): ConfigRepository
