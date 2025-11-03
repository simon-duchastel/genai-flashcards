package ai.solenne.flashcards.shared.api.dto

/**
 * OAuth provider type (Google, Apple, etc).
 * This is separate from OAuthPlatform which indicates the client platform (WEB, IOS).
 */
enum class OAuthProvider {
    GOOGLE,
    APPLE
}
