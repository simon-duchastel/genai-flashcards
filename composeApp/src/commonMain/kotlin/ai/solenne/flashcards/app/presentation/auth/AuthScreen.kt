package ai.solenne.flashcards.app.presentation.auth

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable
import ai.solenne.flashcards.app.parcel.Parcelize

@Serializable
@Parcelize
data object AuthScreen : Screen

// UI State
data class AuthUiState(
    val apiKeyState: ApiKeyState,
    val logInState: LogInState,
    val dangerousModeState: DangerousModeState,
    val deleteAccountModal: DeleteAccountModal,
    val onBackClicked: (() -> Unit)?, // null if back button is not available
    val error: String?,
    val solenneAiExpanded: Boolean,
    val onSolenneAiExpandedToggle: () -> Unit,
    val ownAiExpanded: Boolean,
    val onOwnAiExpandedToggle: () -> Unit,
    val showSolenneAiInfo: Boolean,
    val onSolenneAiInfoToggle: () -> Unit,
    val showOwnAiInfo: Boolean,
    val onOwnAiInfoToggle: () -> Unit,
) : CircuitUiState

sealed interface ApiKeyState {
    data object Loading : ApiKeyState
    data object Empty : ApiKeyState
    data class Loaded(
        val apiKey: String,
        val onApiKeyChanged: (String) -> Unit,
        val onSaveClicked: () -> Unit,
    ) : ApiKeyState
}

sealed interface LogInState {
    data class Loading(
        val loadingGoogle: Boolean,
        val loadingApple: Boolean,
    ) : LogInState

    data class LoggedOut(
        val onGoogleSignInClicked: () -> Unit,
        val onAppleSignInClicked: () -> Unit,
        val dangerousModeState: DangerousModeState,
        val onDeleteAccountClicked: () -> Unit,
    ) : LogInState

    data class LoggedIn(
        val onLogoutClicked: () -> Unit,
    ) : LogInState
}

sealed interface DangerousModeState {
    data class Disabled(
        val onDangerousModeToggled: () -> Unit,
    ) : DangerousModeState

    data class Enabled(
        val onDangerousModeToggled: () -> Unit,
        val onDeleteAccountClicked: () -> Unit,
    ) : DangerousModeState
}

sealed interface DeleteAccountModal {
    data object Hidden : DeleteAccountModal
    data class Visible(
        val onDeleteAccountConfirmed: () -> Unit,
        val onDeleteAccountCancelled: () -> Unit,
        val isDeletingAccount: Boolean,
    ) : DeleteAccountModal
}