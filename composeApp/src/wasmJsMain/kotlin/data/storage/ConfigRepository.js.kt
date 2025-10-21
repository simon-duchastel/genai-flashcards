package data.storage

import kotlinx.browser.localStorage

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class ConfigRepositoryJs : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"

    override suspend fun getGeminiApiKey(): String? {
        return localStorage.getItem(apiKeyStorageKey)
    }

    override suspend fun setGeminiApiKey(apiKey: String) {
        localStorage.setItem(apiKeyStorageKey, apiKey)
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryJs()
