package data.storage

import kotlinx.browser.localStorage

/**
 * JavaScript/Browser implementation using localStorage API.
 */
class ConfigRepositoryJs : ConfigRepository {
    private val apiKeyStorageKey = "gemini_api_key"
    private val darkModeStorageKey = "dark_mode"
    private val sessionTokenKey = "session_token"
    private val userEmailKey = "user_email"
    private val userNameKey = "user_name"
    private val userPictureKey = "user_picture"

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

    // User Info
    override suspend fun getUserEmail(): String? {
        return localStorage.getItem(userEmailKey)
    }

    override suspend fun setUserEmail(email: String) {
        localStorage.setItem(userEmailKey, email)
    }

    override suspend fun getUserName(): String? {
        return localStorage.getItem(userNameKey)
    }

    override suspend fun setUserName(name: String) {
        localStorage.setItem(userNameKey, name)
    }

    override suspend fun getUserPicture(): String? {
        return localStorage.getItem(userPictureKey)
    }

    override suspend fun setUserPicture(picture: String?) {
        if (picture != null) {
            localStorage.setItem(userPictureKey, picture)
        } else {
            localStorage.removeItem(userPictureKey)
        }
    }

    override suspend fun clearUserInfo() {
        localStorage.removeItem(userEmailKey)
        localStorage.removeItem(userNameKey)
        localStorage.removeItem(userPictureKey)
    }
}

actual fun getConfigRepository(): ConfigRepository = ConfigRepositoryJs()
