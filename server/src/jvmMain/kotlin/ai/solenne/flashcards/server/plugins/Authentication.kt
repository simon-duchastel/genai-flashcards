package ai.solenne.flashcards.server.plugins

import ai.solenne.flashcards.server.auth.AuthenticatedUser
import ai.solenne.flashcards.server.repository.AuthRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*

/**
 * Configure authentication for the application.
 * Uses Bearer token authentication with session validation.
 */
fun Application.configureAuthentication(authRepository: AuthRepository) {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                val session = authRepository.getSession(tokenCredential.token)

                if (session == null || session.isExpired()) {
                    null
                } else {
                    authRepository.updateSessionAccess(tokenCredential.token)

                    AuthenticatedUser(session.userId)
                }
            }
        }
    }
}
