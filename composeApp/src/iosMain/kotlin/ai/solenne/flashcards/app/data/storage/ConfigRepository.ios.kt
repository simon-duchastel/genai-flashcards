package ai.solenne.flashcards.app.data.storage

import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults for persistence.
 */
class ConfigRepositoryIos : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
    private val sessionTokenStorageKey = "session_token"
    private val oauthProviderStorageKey = "oauth_provider"
    private val userDefaults = NSUserDefaults.standardUserDefaults

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getGeminiApiKey(): String? {
        return userDefaults.stringForKey(apiKeyStorageKey)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setGeminiApiKey(apiKey: String) {
        userDefaults.setObject(apiKey, forKey = apiKeyStorageKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun isDarkMode(): Boolean {
        return userDefaults.stringForKey(darkModeStorageKey) == "true"
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setDarkMode(isDark: Boolean) {
        userDefaults.setObject(isDark.toString(), forKey = darkModeStorageKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getSessionToken(): String? {
        return userDefaults.stringForKey(sessionTokenStorageKey)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setSessionToken(token: String) {
        userDefaults.setObject(token, forKey = sessionTokenStorageKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun clearSessionToken() {
        userDefaults.removeObjectForKey(sessionTokenStorageKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getOAuthProvider(): OAuthProvider? {
        return userDefaults.stringForKey(oauthProviderStorageKey)?.let { providerName ->
            try {
                OAuthProvider.valueOf(providerName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setOAuthProvider(provider: OAuthProvider) {
        userDefaults.setObject(provider.name, forKey = oauthProviderStorageKey)
        userDefaults.synchronize()
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryIos()
