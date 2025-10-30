package presentation.auth

import androidx.compose.runtime.*
import api.dto.OAuthProvider
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import data.auth.OAuthHandler
import data.storage.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import presentation.home.HomeScreen
import presentation.splash.SplashScreen

class AuthPresenter(
    private val navigator: Navigator,
    private val configRepository: ConfigRepository,
    private val oauthHandler: OAuthHandler,
    private val authApiClient: data.api.AuthApiClient
) : Presenter<AuthUiState> {

    @Composable
    override fun present(): AuthUiState {
        val canGoBack = remember { navigator.peekBackStack().size > 1 }
        var apiKeyInput: String? by remember { mutableStateOf(null) }
        var isSaving by remember { mutableStateOf(false) }
        var isAuthenticatingWithGoogle by remember { mutableStateOf(false) }
        var isAuthenticatingWithApple by remember { mutableStateOf(false) }
        var isLoggingOut by remember { mutableStateOf(false) }
        var isLoggedIn by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var isDangerousModeEnabled by remember { mutableStateOf(false) }
        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var isDeletingAccount by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            apiKeyInput = configRepository.getGeminiApiKey() ?: ""

            // Check if user is logged in and load persisted user info
            val sessionToken = configRepository.getSessionToken()
            if (sessionToken != null) {
                isLoggedIn = true
            } else {
                isLoggedIn = false
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
                scope.handleOAuthSignIn(
                    provider = OAuthProvider.GOOGLE,
                    setAuthenticating = { isAuthenticatingWithGoogle = it },
                    setError = { error = it },
                    setLoggedIn = { isLoggedIn = it },
                )
            },
            isAuthenticatingWithApple = isAuthenticatingWithApple,
            onAppleSignInClicked = {
                scope.handleOAuthSignIn(
                    provider = OAuthProvider.APPLE,
                    setAuthenticating = { isAuthenticatingWithApple = it },
                    setError = { error = it },
                    setLoggedIn = { isLoggedIn = it },
                )
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
                        // Clear session
                        configRepository.clearSessionToken()

                        // Update state
                        isLoggedIn = false
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
            isDangerousModeEnabled = isDangerousModeEnabled,
            onDangerousModeToggled = {
                isDangerousModeEnabled = !isDangerousModeEnabled
            },
            showDeleteAccountDialog = showDeleteAccountDialog,
            onDeleteAccountClicked = {
                showDeleteAccountDialog = true
            },
            onDeleteAccountConfirmed = {
                showDeleteAccountDialog = false
                isDeletingAccount = true
                error = null
                scope.launch {
                    try {
                        val sessionToken = configRepository.getSessionToken()
                        if (sessionToken != null) {
                            authApiClient.deleteAccount(sessionToken)
                        } else {
                            throw IllegalStateException("Session token is null")
                        }
                        // Clear session
                        configRepository.clearSessionToken()

                        // Update state
                        isLoggedIn = false
                        isDeletingAccount = false

                        // go back to splash screen
                        navigator.resetRoot(SplashScreen)
                    } catch (e: Exception) {
                        error = """
                            Account deletion failed: ${e.message}

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                        isDeletingAccount = false
                    }
                }
            },
            onDeleteAccountCancelled = {
                showDeleteAccountDialog = false
            },
            isDeletingAccount = isDeletingAccount,
        )
    }

    /**
     * Generic OAuth sign-in handler that works for any provider.
     */
    private fun CoroutineScope.handleOAuthSignIn(
        provider: OAuthProvider,
        setAuthenticating: (Boolean) -> Unit,
        setError: (String?) -> Unit,
        setLoggedIn: (Boolean) -> Unit,
    ) {
        setAuthenticating(true)
        setError(null)
        launch {
            try {
                val authResponse = oauthHandler.startOAuthFlow(provider)
                if (authResponse == null) {
                    setError(
                        """
                        ${
                            provider.name.lowercase().replaceFirstChar { it.uppercase() }
                        } sign-in failed. Please try again or enter an API key below.

                        Need help? Email help@solenne.ai
                    """.trimIndent()
                    )
                    setAuthenticating(false)
                    return@launch
                }

                // Save session token
                configRepository.setSessionToken(authResponse.sessionToken)

                // Update state
                setLoggedIn(true)

                // Navigate to home
                navigator.resetRoot(HomeScreen)
            } catch (_: Exception) {
                setError(
                    """
                    ${
                        provider.name.lowercase().replaceFirstChar { it.uppercase() }
                    } sign-in failed. Please try again or use API key below.

                    Need help? Email help@solenne.ai
                """.trimIndent()
                )
                setAuthenticating(false)
            }
        }
    }
}
