package data.storage

/**
 * Platform-specific storage for configuration values.
 */
interface ConfigRepository {
    // Gemini API Key
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(apiKey: String)

    // Theme
    suspend fun isDarkMode(): Boolean
    suspend fun setDarkMode(isDark: Boolean)

    // Session Management
    suspend fun getSessionToken(): String?
    suspend fun setSessionToken(token: String)
    suspend fun clearSessionToken()

    // User Info (for display)
    suspend fun getUserEmail(): String?
    suspend fun setUserEmail(email: String)
    suspend fun getUserName(): String?
    suspend fun setUserName(name: String)
    suspend fun getUserPicture(): String?
    suspend fun setUserPicture(picture: String?)
    suspend fun clearUserInfo()
}

expect fun getConfigRepository(): ConfigRepository
