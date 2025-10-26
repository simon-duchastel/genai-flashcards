package com.flashcards.server.routes

import api.dto.ErrorResponse
import api.dto.LoginUrlResponse
import api.dto.MeResponse
import api.routes.ApiRoutes
import com.flashcards.server.auth.AuthenticatedUser
import com.flashcards.server.auth.GoogleOAuthService
import com.flashcards.server.repository.AuthRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Configure authentication routes.
 */
fun Route.authRoutes(
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService,
) {
    // GET /api/v1/auth/google/login?redirect=true - Get Google OAuth URL, redirect if redirect=true
    // redirect is an optional query parameter
    get(ApiRoutes.AUTH_GOOGLE_LOGIN) {
        val redirect = call.parameters["redirect"] == "true"
        val authUrl = googleOAuthService.getAuthorizationUrl()

        if (redirect) {
            call.respondRedirect(authUrl)
        } else {
            call.respond(LoginUrlResponse(authUrl))
        }
    }

    // GET /api/v1/auth/google/callback?code=xxx - OAuth callback
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK) {
        val code = call.parameters["code"]
            ?: return@get call.respondRedirect("/auth?error=missing_code")

        try {
            // Exchange code for user info
            val userFromGoogle = googleOAuthService.exchangeCodeForUser(code)

            // Check if user already exists
            val user = authRepository.getUserByAuthId(userFromGoogle.authId)
                ?: authRepository.createUser(userFromGoogle)

            // Create session
            val session = authRepository.createSession(user.userId)

            // Redirect with token and optional user info in query params
            val redirectUrl = buildString {
                append("${ApiRoutes.WEB_CLIENT}/redirect?token=${session.sessionToken}")
                append("&email=${user.email}")
                user.name?.let { append("&name=${it}") }
                user.picture?.let { append("&picture=${it}") }
            }

            call.respondRedirect(redirectUrl)
        } catch (e: Exception) {
            // Redirect to auth screen with error
            call.application.log.error("OAuth callback error", e)
            call.respondRedirect("${ApiRoutes.WEB_CLIENT}/auth?error='unable-to-create'")
        }
    }

    // Protected routes (require authentication)
    authenticate("auth-bearer") {
        // POST /api/v1/auth/logout - Logout
        post(ApiRoutes.AUTH_LOGOUT) {
            call.principal<AuthenticatedUser>()
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
