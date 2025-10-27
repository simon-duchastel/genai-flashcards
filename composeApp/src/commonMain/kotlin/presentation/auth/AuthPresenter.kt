package presentation.auth

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import data.auth.GoogleOAuthHandler
import data.storage.ConfigRepository
import kotlinx.coroutines.launch
import presentation.home.HomeScreen
import presentation.splash.SplashScreen

class AuthPresenter(
    private val navigator: Navigator,
    private val configRepository: ConfigRepository,
    private val googleOAuthHandler: GoogleOAuthHandler,
    private val authApiClient: data.api.AuthApiClient
) : Presenter<AuthUiState> {

    @Composable
    override fun present(): AuthUiState {
        val canGoBack = remember { navigator.peekBackStack().size > 1 }
        var apiKeyInput: String? by remember { mutableStateOf(null) }
        var isSaving by remember { mutableStateOf(false) }
        var isAuthenticatingWithGoogle by remember { mutableStateOf(false) }
        var isLoggingOut by remember { mutableStateOf(false) }
        var isLoggedIn by remember { mutableStateOf(false) }
        var currentUserName by remember { mutableStateOf<String?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            apiKeyInput = configRepository.getGeminiApiKey() ?: ""

            // Check if user is logged in and load persisted user info
            val sessionToken = configRepository.getSessionToken()
            if (sessionToken != null) {
                isLoggedIn = true
                val userName = configRepository.getUserName()
                val userEmail = configRepository.getUserEmail()
                currentUserName = userName ?: userEmail
            } else {
                isLoggedIn = false
                currentUserName = null
            }
        }

        return AuthUiState(
            apiKeyInput = apiKeyInput,
            onBackClicked = if (canGoBack) { { navigator.pop() } } else null,
            isSaving = isSaving,
            error = error,
            onApiKeyChanged = { newValue ->
                apiKeyInput = newValue
                error = null
            },
            onSaveClicked = {
                val apiKeyToSubmit = apiKeyInput
                when {
                    apiKeyToSubmit.isNullOrBlank() -> {
                        error = """
                            Please enter an API key.

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                    }
                    else -> {
                        isSaving = true
                        error = null
                        scope.launch {
                            try {
                                configRepository.setGeminiApiKey(apiKeyToSubmit.trim())
                                navigator.resetRoot(SplashScreen)
                            } catch (e: Exception) {
                                error = """
                                    Failed to save API key: ${e.message}

                                    Need help? Email help@solenne.ai
                                """.trimIndent()
                                isSaving = false
                            }
                        }
                    }
                }
            },
            isAuthenticatingWithGoogle = isAuthenticatingWithGoogle,
            onGoogleSignInClicked = {
                isAuthenticatingWithGoogle = true
                error = null
                scope.launch {
                    try {
                        val authResponse = googleOAuthHandler.startOAuthFlow()
                        if (authResponse == null) {
                            error = """
                                Google sign-in failed. Please try again or enter an API key below.

                                Need help? Email help@solenne.ai
                            """.trimIndent()
                            isAuthenticatingWithGoogle = false
                            return@launch
                        }

                        // Save session token and user info
                        configRepository.setSessionToken(authResponse.sessionToken)
                        configRepository.setUserEmail(authResponse.user.email)
                        authResponse.user.name?.let { configRepository.setUserName(it) }
                        authResponse.user.picture?.let { configRepository.setUserPicture(it) }

                        // Update state
                        isLoggedIn = true
                        currentUserName = authResponse.user.name ?: authResponse.user.email

                        // Navigate to home
                        navigator.resetRoot(HomeScreen)
                    } catch (_: Exception) {
                        error = """
                            Google sign-in failed. Please try again or use API key below.

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                        isAuthenticatingWithGoogle = false
                    }
                }
            },
            isLoggedIn = isLoggedIn,
            isLoggingOut = isLoggingOut,
            onLogoutClicked = {
                isLoggingOut = true
                error = null
                scope.launch {
                    try {
                        val sessionToken = configRepository.getSessionToken()
                        if (sessionToken != null) {
                            authApiClient.logout(sessionToken)
                        }
                        // Clear session and user info
                        configRepository.clearSessionToken()
                        configRepository.clearUserInfo()

                        // Update state
                        isLoggedIn = false
                        currentUserName = null
                        isLoggingOut = false

                        // go back to splash screen
                        navigator.resetRoot(SplashScreen)
                    } catch (e: Exception) {
                        error = """
                            Logout failed: ${e.message}

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                        isLoggingOut = false
                    }
                }
            },
            currentUserName = currentUserName
        )
    }
}
