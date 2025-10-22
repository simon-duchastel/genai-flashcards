package com.flashcards.server.routes

import api.dto.AuthResponse
import api.dto.ErrorResponse
import api.dto.LoginUrlResponse
import api.dto.MeResponse
import api.routes.ApiRoutes
import com.flashcards.server.auth.AuthenticatedUser
import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.repository.AuthRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure authentication routes.
 */
fun Route.authRoutes(
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService
) {
    // GET /api/v1/auth/google/login - Get Google OAuth URL
    get(ApiRoutes.AUTH_GOOGLE_LOGIN) {
        val authUrl = googleOAuthService.getAuthorizationUrl()
        call.respond(HttpStatusCode.OK, LoginUrlResponse(authUrl))
    }

    // GET /api/v1/auth/google/callback?code=xxx - OAuth callback
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK) {
        val code = call.parameters["code"]
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing authorization code", "MISSING_CODE")
            )

        try {
            // Exchange code for user info
            val userFromGoogle = googleOAuthService.exchangeCodeForUser(code)

            // Check if user already exists
            val user = authRepository.getUserByAuthId(userFromGoogle.authId)
                ?: authRepository.createUser(userFromGoogle)

            // Create session
            val session = authRepository.createSession(user.userId)

            // Return session token and user
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    sessionToken = session.sessionToken,
                    user = user
                )
            )
        } catch (e: Exception) {
            call.application.log.error("OAuth callback error", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Authentication failed: ${e.message}", "AUTH_FAILED")
            )
        }
    }

    // Protected routes (require authentication)
    authenticate("auth-bearer") {
        // POST /api/v1/auth/logout - Logout
        post(ApiRoutes.AUTH_LOGOUT) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            // Get session token from Authorization header
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            if (token != null) {
                authRepository.invalidateSession(token)
            }

            call.respond(HttpStatusCode.NoContent)
        }

        // GET /api/v1/auth/me - Get current user
        get(ApiRoutes.AUTH_ME) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val user = authRepository.getUserById(principal.userId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("User not found", "USER_NOT_FOUND")
                )

            call.respond(HttpStatusCode.OK, MeResponse(user))
        }
    }
}
