package data.storage

import kotlinx.browser.localStorage

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class ConfigRepositoryJs : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"

    override suspend fun getGeminiApiKey(): String? {
        return localStorage.getItem(apiKeyStorageKey)
    }

    override suspend fun setGeminiApiKey(apiKey: String) {
        localStorage.setItem(apiKeyStorageKey, apiKey)
    }

    override suspend fun isDarkMode(): Boolean {
        return localStorage.getItem(darkModeStorageKey) == "true"
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        localStorage.setItem(darkModeStorageKey, isDark.toString())
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryJs()
