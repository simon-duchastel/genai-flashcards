package presentation.auth

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable
import parcel.Parcelize

@Serializable
@Parcelize
data object AuthScreen : Screen

// UI State
data class AuthUiState(
    val apiKeyInput: String?,
    val onBackClicked: (() -> Unit)?, // null if back button is not available
    val isSaving: Boolean,
    val error: String?,
    val onApiKeyChanged: (String) -> Unit,
    val onSaveClicked: () -> Unit
) : CircuitUiState
