package ai.solenne.flashcards.app.data.repository

import ai.solenne.flashcards.app.data.storage.ConfigRepository
import ai.solenne.flashcards.app.domain.repository.AuthRepository

/**
 * Implementation of AuthRepository that wraps ConfigRepository.
 */
class AuthRepositoryImpl(
    private val configRepository: ConfigRepository,
) : AuthRepository {

    override suspend fun isSignedIn(): Boolean {
        return configRepository.getSessionToken() != null
    }

    override suspend fun getSessionToken(): String? {
        return configRepository.getSessionToken()
    }
}
