package data.storage

/**
 * Platform-specific storage for configuration values.
 */
interface ConfigRepository {
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(apiKey: String)
}

expect fun getConfigRepository(): ConfigRepository
