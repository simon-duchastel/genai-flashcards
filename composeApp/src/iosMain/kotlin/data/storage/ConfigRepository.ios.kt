package data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults for persistence.
 */
class ConfigRepositoryIos : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
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
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryIos()
