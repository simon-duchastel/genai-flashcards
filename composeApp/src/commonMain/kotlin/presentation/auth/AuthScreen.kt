package presentation.auth

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable

@Serializable
data object AuthScreen : Screen

// UI State
data class AuthUiState(
    val apiKeyInput: String?,
    val onBackClicked: (() -> Unit)?, // null if back button is not available
    val isSaving: Boolean,
    val error: String?,
    val onApiKeyChanged: (String) -> Unit,
    val onSaveClicked: () -> Unit,
    // Google OAuth
    val isAuthenticatingWithGoogle: Boolean,
    val onGoogleSignInClicked: () -> Unit,
    // Apple OAuth
    val isAuthenticatingWithApple: Boolean,
    val onAppleSignInClicked: () -> Unit,
    // Logout
    val isLoggedIn: Boolean,
    val isLoggingOut: Boolean,
    val onLogoutClicked: () -> Unit,
) : CircuitUiState
