package data.auth

import api.dto.AuthResponse
import data.api.AuthApiClient

/**
 * Web implementation of AppleOAuthHandler.
 * Currently not implemented - Apple OAuth for web requires different setup.
 * For MVP, we focus on iOS implementation only.
 */
class WebAppleOAuthHandler(
    private val authApiClient: AuthApiClient
): AppleOAuthHandler {
    override suspend fun startOAuthFlow(): AuthResponse? {
        println("Apple OAuth on web is not yet implemented")
        return null
    }
}

actual fun getAppleOAuthHandler(authApiClient: AuthApiClient): AppleOAuthHandler {
    return WebAppleOAuthHandler(authApiClient)
}
