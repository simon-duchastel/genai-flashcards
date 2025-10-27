package com.flashcards.server.routes

import api.dto.ErrorResponse
import api.dto.LoginUrlResponse
import api.dto.MeResponse
import api.dto.OAuthPlatform
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
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Configure authentication routes.
 */
fun Route.authRoutes(
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService,
    testGoogleOAuthService: GoogleOAuthService,
) {
    /**
     * Handles OAuth login by getting authorization URL and responding with it.
     */
    suspend fun RoutingContext.handleOAuthLogin(isTest: Boolean, platform: OAuthPlatform) {
        val authUrl = if (isTest){
            testGoogleOAuthService.getAuthorizationUrl(platform)
        } else {
            googleOAuthService.getAuthorizationUrl(platform)
        }

        call.respond(LoginUrlResponse(authUrl))
    }

    /**
     * Handles OAuth callback by exchanging code for user info and creating session.
     */
    suspend fun RoutingContext.handleOAuthCallback(
        isTest: Boolean,
        platform: OAuthPlatform,
        redirectUrl: String,
    ) {
        val code = call.parameters["code"]
            ?: return call.respondRedirect("$redirectUrl?auth-redirect=true&error=missing_code")

        try {
            // Exchange code for user info
            val userFromGoogle = if (isTest) {
                testGoogleOAuthService.exchangeCodeForUser(code, platform)
            } else {
                googleOAuthService.exchangeCodeForUser(code, platform)
            }

            // Check if user already exists
            val user = authRepository.getUserByAuthId(userFromGoogle.authId)
                ?: authRepository.createUser(userFromGoogle)

            // Create session
            val session = authRepository.createSession(user.userId)

            // Redirect with token and optional user info in query params
            val redirectUrl = buildString {
                append("$redirectUrl?auth-redirect=true&token=${session.sessionToken}")
            }

            call.respondRedirect(redirectUrl)
        } catch (e: Exception) {
            // Redirect to auth screen with error
            call.application.log.error("OAuth callback error", e)
            call.respondRedirect("$redirectUrl?auth-redirect=true&error='unable-to-create'")
        }
    }

    // GET /api/v1/auth/google/login - Get Google OAuth URL
    get(ApiRoutes.AUTH_GOOGLE_LOGIN) {
        val platform = if (call.request.queryParameters["platform"] == "ios") {
            OAuthPlatform.IOS
        } else {
            OAuthPlatform.WEB
        }
        handleOAuthLogin(isTest = false, platform = platform)
    }

    // GET /api/v1/auth/google/test/login - Get Google OAuth URL for testing
    get(ApiRoutes.AUTH_GOOGLE_LOGIN_TEST) {
        val platform = if (call.request.queryParameters["platform"] == "ios") {
            OAuthPlatform.IOS
        } else {
            OAuthPlatform.WEB
        }
        handleOAuthLogin(isTest = true, platform = platform)
    }

    // GET /api/v1/auth/google/callback?code=xxx - OAuth callback
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK) {
        handleOAuthCallback(isTest = false, platform = OAuthPlatform.WEB, redirectUrl = ApiRoutes.WEB_CLIENT)
    }

    // GET /api/v1/auth/google/callback/ios?code=xxx - OAuth callback for iOS
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK_IOS) {
        handleOAuthCallback(isTest = false, platform = OAuthPlatform.IOS, redirectUrl = ApiRoutes.IOS_CLIENT)
    }

    // GET /api/v1/auth/google/test/callback?code=xxx - OAuth callback for testing
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK_TEST) {
        handleOAuthCallback(isTest = true, platform = OAuthPlatform.WEB, redirectUrl = ApiRoutes.TEST_WEB_CLIENT)
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
