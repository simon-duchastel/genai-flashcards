package ai.solenne.flashcards.app.data.storage

import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import kotlinx.browser.localStorage

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class ConfigRepositoryJs : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
    private val sessionTokenKey = "session_token"
    private val oauthProviderKey = "oauth_provider"

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

    override suspend fun getOAuthProvider(): OAuthProvider? {
        return localStorage.getItem(oauthProviderKey)?.let { providerName ->
            try {
                OAuthProvider.valueOf(providerName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    override suspend fun setOAuthProvider(provider: OAuthProvider) {
        localStorage.setItem(oauthProviderKey, provider.name)
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryJs()
