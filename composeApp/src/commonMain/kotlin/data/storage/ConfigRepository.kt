package data.storage

/**
 * Platform-specific storage for configuration values.
 */
interface ConfigRepository {
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(apiKey: String)
    suspend fun isDarkMode(): Boolean
    suspend fun setDarkMode(isDark: Boolean)
}

expect fun getConfigRepository(): ConfigRepository
