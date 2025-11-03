package ai.solenne.flashcards.app.data.storage

import kotlinx.browser.localStorage

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class ConfigRepositoryJs : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
    private val sessionTokenKey = "session_token"

    // Gemini API Key
    override suspend fun getGeminiApiKey(): String? {
        return localStorage.getItem(apiKeyStorageKey)
    }

    override suspend fun setGeminiApiKey(apiKey: String) {
        localStorage.setItem(apiKeyStorageKey, apiKey)
    }

    // Theme
    override suspend fun isDarkMode(): Boolean {
        return localStorage.getItem(darkModeStorageKey) == "true"
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        localStorage.setItem(darkModeStorageKey, isDark.toString())
    }

    // Session Management
    override suspend fun getSessionToken(): String? {
        return localStorage.getItem(sessionTokenKey)
    }

    override suspend fun setSessionToken(token: String) {
        localStorage.setItem(sessionTokenKey, token)
    }

    override suspend fun clearSessionToken() {
        localStorage.removeItem(sessionTokenKey)
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryJs()
