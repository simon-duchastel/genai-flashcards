package ai.solenne.flashcards.server.routes

import ai.solenne.flashcards.shared.api.dto.ErrorResponse
import ai.solenne.flashcards.shared.api.dto.LoginUrlResponse
import ai.solenne.flashcards.shared.api.dto.MeResponse
import ai.solenne.flashcards.shared.api.dto.OAuthPlatform
import ai.solenne.flashcards.shared.api.routes.ApiRoutes
import ai.solenne.flashcards.server.auth.AppleOAuthService
import ai.solenne.flashcards.server.auth.AuthenticatedUser
import ai.solenne.flashcards.server.auth.GoogleOAuthService
import ai.solenne.flashcards.server.repository.AuthRepository
import ai.solenne.flashcards.shared.domain.model.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Configure authentication routes.
 */
fun Route.authRoutes(
    authRepository: AuthRepository,
    googleOAuthService: GoogleOAuthService,
    testGoogleOAuthService: GoogleOAuthService,
    appleOAuthService: AppleOAuthService,
    storage: ai.solenne.flashcards.server.storage.FirestoreStorage,
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
     * Generic function that works for any OAuth provider.
     */
    suspend fun RoutingContext.handleOAuthCallback(
        exchangeCodeForUser: suspend (String, OAuthPlatform) -> User,
        platform: OAuthPlatform,
        redirectUrl: String,
    ) {
        val code = call.parameters["code"]
            ?: return call.respondRedirect("$redirectUrl?auth-redirect=true&error=missing_code")

        try {
            // Exchange code for user info using the provided function
            val userFromOAuth = exchangeCodeForUser(code, platform)

            // Check if user already exists
            val user = authRepository.getUserByAuthId(userFromOAuth.authId)
                ?: authRepository.createUser(userFromOAuth)

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
        val platform = when (call.request.queryParameters["platform"]) {
            "ios" -> OAuthPlatform.IOS
            "android" -> OAuthPlatform.ANDROID
            else -> OAuthPlatform.WEB
        }
        handleOAuthLogin(isTest = false, platform = platform)
    }

    // GET /api/v1/auth/google/test/login - Get Google OAuth URL for testing
    get(ApiRoutes.AUTH_GOOGLE_LOGIN_TEST) {
        val platform = when (call.request.queryParameters["platform"]) {
            "ios" -> OAuthPlatform.IOS
            "android" -> OAuthPlatform.ANDROID
            else -> OAuthPlatform.WEB
        }
        handleOAuthLogin(isTest = true, platform = platform)
    }

    // GET /api/v1/auth/google/callback?code=xxx - OAuth callback
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> googleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.WEB,
            redirectUrl = ApiRoutes.WEB_CLIENT
        )
    }

    // GET /api/v1/auth/google/callback/ios?code=xxx - OAuth callback for iOS
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK_IOS) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> googleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.IOS,
            redirectUrl = ApiRoutes.MOBILE_CLIENTS
        )
    }

    // GET /api/v1/auth/google/callback/android?code=xxx - OAuth callback for iOS
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK_ANDROID) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> googleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.ANDROID,
            redirectUrl = ApiRoutes.MOBILE_CLIENTS
        )
    }

    // GET /api/v1/auth/google/test/callback?code=xxx - OAuth callback for testing
    get(ApiRoutes.AUTH_GOOGLE_CALLBACK_TEST) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> testGoogleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.WEB,
            redirectUrl = ApiRoutes.TEST_WEB_CLIENT
        )
    }

    // GET /api/v1/auth/apple/login - Get Apple OAuth URL
    get(ApiRoutes.AUTH_APPLE_LOGIN) {
        val platform = when (call.request.queryParameters["platform"]) {
            "ios" -> OAuthPlatform.IOS
            "android" -> OAuthPlatform.ANDROID
            else -> OAuthPlatform.WEB
        }
        val authUrl = appleOAuthService.getAuthorizationUrl(platform)
        call.respond(LoginUrlResponse(authUrl))
    }

    // GET /api/v1/auth/apple/callback?code=xxx - OAuth callback for Apple
    get(ApiRoutes.AUTH_APPLE_CALLBACK) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> appleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.WEB,
            redirectUrl = ApiRoutes.WEB_CLIENT
        )
    }

    // GET /api/v1/auth/apple/callback/ios?code=xxx - OAuth callback for Apple iOS
    get(ApiRoutes.AUTH_APPLE_CALLBACK_IOS) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> appleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.IOS,
            redirectUrl = ApiRoutes.MOBILE_CLIENTS
        )
    }

    // GET /api/v1/auth/apple/callback/android?code=xxx - OAuth callback for Apple Android
    get(ApiRoutes.AUTH_APPLE_CALLBACK_ANDROID) {
        handleOAuthCallback(
            exchangeCodeForUser = { code, platform -> appleOAuthService.exchangeCodeForUser(code, platform) },
            platform = OAuthPlatform.ANDROID,
            redirectUrl = ApiRoutes.MOBILE_CLIENTS
        )
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

        // DELETE /api/v1/auth/account - Delete current user account
        delete(ApiRoutes.AUTH_DELETE_ACCOUNT) {
            val principal = call.principal<AuthenticatedUser>()
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Authentication required", "UNAUTHORIZED")
                )

            val userId = principal.userId

            try {
                storage.deleteAllByUserId(userId)
                authRepository.deleteUserAccount(userId)

                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                call.application.log.error("Failed to delete user account: $userId", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Failed to delete account", "DELETE_FAILED")
                )
            }
        }
    }
}
