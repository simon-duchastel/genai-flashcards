package data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults for persistence.
 */
class ConfigRepositoryIos : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
    private val sessionTokenStorageKey = "session_token"
    private val userEmailKey = "user_email"
    private val userNameKey = "user_name"
    private val userPictureKey = "user_picture"
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

    // User Info
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getUserEmail(): String? {
        return userDefaults.stringForKey(userEmailKey)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setUserEmail(email: String) {
        userDefaults.setObject(email, forKey = userEmailKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getUserName(): String? {
        return userDefaults.stringForKey(userNameKey)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setUserName(name: String) {
        userDefaults.setObject(name, forKey = userNameKey)
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getUserPicture(): String? {
        return userDefaults.stringForKey(userPictureKey)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun setUserPicture(picture: String?) {
        if (picture != null) {
            userDefaults.setObject(picture, forKey = userPictureKey)
        } else {
            userDefaults.removeObjectForKey(userPictureKey)
        }
        userDefaults.synchronize()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun clearUserInfo() {
        userDefaults.removeObjectForKey(userEmailKey)
        userDefaults.removeObjectForKey(userNameKey)
        userDefaults.removeObjectForKey(userPictureKey)
        userDefaults.synchronize()
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryIos()
