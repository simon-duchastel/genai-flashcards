package data.storage

/**
 * Platform-specific storage for configuration values.
 */
interface ConfigRepository {
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(apiKey: String)
    suspend fun isDarkMode(): Boolean
    suspend fun setDarkMode(isDark: Boolean)

    // Session token for authenticated API calls
    suspend fun getSessionToken(): String?
    suspend fun setSessionToken(token: String)
    suspend fun clearSessionToken()
}

expect fun getConfigRepository(): ConfigRepository
