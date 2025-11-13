package ai.solenne.flashcards.app.presentation.auth

import androidx.compose.runtime.*
import ai.solenne.flashcards.shared.api.dto.OAuthProvider
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import ai.solenne.flashcards.app.data.auth.OAuthHandler
import ai.solenne.flashcards.app.data.api.AuthApiClient
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ai.solenne.flashcards.app.presentation.home.HomeScreen
import ai.solenne.flashcards.app.presentation.splash.SplashScreen

class AuthPresenter(
    private val navigator: Navigator,
    private val configRepository: ConfigRepository,
    private val oauthHandler: OAuthHandler,
    private val authApiClient: AuthApiClient
) : Presenter<AuthUiState> {

    @Composable
    override fun present(): AuthUiState {
        val canGoBack = remember { navigator.peekBackStack().size > 1 }
        var apiKeyInput: String? by remember { mutableStateOf(null) }
        var originalApiKey by remember { mutableStateOf("") }
        var isAuthenticatingWithGoogle by remember { mutableStateOf(false) }
        var isAuthenticatingWithApple by remember { mutableStateOf(false) }
        var isLoggingOut by remember { mutableStateOf(false) }
        var isLoggedIn by remember { mutableStateOf(false) }
        var loggedInProvider by remember { mutableStateOf<OAuthProvider?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var isDangerousModeEnabled by remember { mutableStateOf(false) }
        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var isDeletingAccount by remember { mutableStateOf(false) }
        var solenneAiExpanded by remember { mutableStateOf<Boolean?>(null) }
        var ownAiExpanded by remember { mutableStateOf<Boolean?>(null) }
        var showSolenneAiInfo by remember { mutableStateOf(false) }
        var showOwnAiInfo by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val loadedApiKey = configRepository.getGeminiApiKey() ?: ""
            apiKeyInput = loadedApiKey
            originalApiKey = loadedApiKey

            // Check if user is logged in and load persisted user info
            val sessionToken = configRepository.getSessionToken()
            isLoggedIn = sessionToken != null

            // Auto-open based on what's active
            if (solenneAiExpanded == null && ownAiExpanded == null) {
                if (isLoggedIn) {
                    solenneAiExpanded = true
                    ownAiExpanded = false
                } else if (loadedApiKey.isNotBlank()) {
                    solenneAiExpanded = false
                    ownAiExpanded = true
                } else {
                    solenneAiExpanded = false
                    ownAiExpanded = false
                }
            }
        }

        val apiKey = apiKeyInput
        val apiKeyState = when (apiKey) {
            null -> ApiKeyState.Loading
            else -> {
                val currentKey = apiKey
                val hasChanged = currentKey != originalApiKey

                if (!hasChanged) {
                    // Key hasn't changed - show loaded state
                    ApiKeyState.Loaded(
                        apiKey = currentKey,
                        currentlyUsingApiKey = originalApiKey.isNotBlank(),
                        onApiKeyChanged = { newValue ->
                            apiKeyInput = newValue
                            error = null
                        },
                        onRemoveClicked = {
                            error = null
                            scope.launch {
                                try {
                                    configRepository.setGeminiApiKey("")
                                    apiKeyInput = ""
                                    originalApiKey = ""
                                    navigator.resetRoot(SplashScreen)
                                } catch (e: Exception) {
                                    error = """
                                        Failed to save API key: ${e.message}

                                        Need help? Email help@solenne.ai
                                    """.trimIndent()
                                }
                            }
                        }
                    )
                } else {
                    // Key has changed - show modified state
                    ApiKeyState.Modified(
                        apiKey = currentKey,
                        currentlyUsingApiKey = originalApiKey.isNotBlank(),
                        onApiKeyChanged = { newValue ->
                            apiKeyInput = newValue
                            error = null
                        },
                        onButtonClicked = {
                            error = null
                            scope.launch {
                                try {
                                    configRepository.setGeminiApiKey(currentKey.trim())
                                    apiKeyInput = currentKey.trim()
                                    originalApiKey = currentKey.trim()
                                    navigator.resetRoot(SplashScreen)
                                } catch (e: Exception) {
                                    error = """
                                        Failed to save API key: ${e.message}

                                        Need help? Email help@solenne.ai
                                    """.trimIndent()
                                }
                            }
                        },
                        onRemoveClicked = {
                            error = null
                            scope.launch {
                                try {
                                    configRepository.setGeminiApiKey("")
                                    apiKeyInput = ""
                                    originalApiKey = ""
                                    navigator.resetRoot(SplashScreen)
                                } catch (e: Exception) {
                                    error = """
                                        Failed to save API key: ${e.message}

                                        Need help? Email help@solenne.ai
                                    """.trimIndent()
                                }
                            }
                        }
                    )
                }
            }
        }

        val dangerousModeState = if (isDangerousModeEnabled) {
            DangerousModeState.Enabled(
                onDangerousModeToggled = { isDangerousModeEnabled = !isDangerousModeEnabled },
                onDeleteAccountClicked = { showDeleteAccountDialog = true }
            )
        } else {
            DangerousModeState.Disabled(
                onDangerousModeToggled = { isDangerousModeEnabled = !isDangerousModeEnabled }
            )
        }

        val logInState = when {
            isAuthenticatingWithGoogle || isAuthenticatingWithApple || isLoggingOut -> LogInState.Loading(
                loadingGoogle = isAuthenticatingWithGoogle,
                loadingApple = isAuthenticatingWithApple,
                loadingLogout = isLoggingOut,
            )
            isLoggedIn -> {
                val loginType = when (loggedInProvider) {
                    OAuthProvider.GOOGLE -> LogInState.LoggedIn.LoginType.SignedInWithGoogle
                    OAuthProvider.APPLE -> LogInState.LoggedIn.LoginType.SignedInWithApple
                    null -> LogInState.LoggedIn.LoginType.SignedInWithGoogle // Default if unknown
                }
                LogInState.LoggedIn(
                    loginType = loginType,
                    onLogoutClicked = {
                        error = null
                        isLoggingOut = true
                        scope.launch {
                            try {
                                val sessionToken = configRepository.getSessionToken()
                                if (sessionToken != null) {
                                    authApiClient.logout(sessionToken)
                                }
                                configRepository.clearSessionToken()
                                isLoggedIn = false
                                loggedInProvider = null
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
                    }
                )
            }
            else -> LogInState.LoggedOut(
                onGoogleSignInClicked = {
                    scope.handleOAuthSignIn(
                        provider = OAuthProvider.GOOGLE,
                        setAuthenticating = { isAuthenticatingWithGoogle = it },
                        setError = { error = it },
                        setLoggedIn = { isLoggedIn = it },
                        setLoggedInProvider = { loggedInProvider = it },
                    )
                },
                onAppleSignInClicked = {
                    scope.handleOAuthSignIn(
                        provider = OAuthProvider.APPLE,
                        setAuthenticating = { isAuthenticatingWithApple = it },
                        setError = { error = it },
                        setLoggedIn = { isLoggedIn = it },
                        setLoggedInProvider = { loggedInProvider = it },
                    )
                },
                dangerousModeState = dangerousModeState,
                onDeleteAccountClicked = { showDeleteAccountDialog = true }
            )
        }

        val deleteAccountModal = if (showDeleteAccountDialog) {
            DeleteAccountModal.Visible(
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
                            configRepository.clearSessionToken()

                            isLoggedIn = false
                            isDeletingAccount = false

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
                isDeletingAccount = isDeletingAccount
            )
        } else {
            DeleteAccountModal.Hidden
        }

        return AuthUiState(
            apiKeyState = apiKeyState,
            logInState = logInState,
            dangerousModeState = dangerousModeState,
            deleteAccountModal = deleteAccountModal,
            onBackClicked = if (canGoBack) { { navigator.pop() } } else null,
            error = error,
            solenneAiExpanded = solenneAiExpanded ?: false,
            onSolenneAiExpandedToggle = {
                solenneAiExpanded = !(solenneAiExpanded ?: false)
                // Close the other section when opening this one
                if (solenneAiExpanded == true) {
                    ownAiExpanded = false
                }
            },
            ownAiExpanded = ownAiExpanded ?: false,
            onOwnAiExpandedToggle = {
                ownAiExpanded = !(ownAiExpanded ?: false)
                // Close the other section when opening this one
                if (ownAiExpanded == true) {
                    solenneAiExpanded = false
                }
            },
            showSolenneAiInfo = showSolenneAiInfo,
            onSolenneAiInfoToggle = { showSolenneAiInfo = !showSolenneAiInfo },
            showOwnAiInfo = showOwnAiInfo,
            onOwnAiInfoToggle = { showOwnAiInfo = !showOwnAiInfo },
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
        setLoggedInProvider: (OAuthProvider?) -> Unit,
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
                setLoggedInProvider(provider)

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
