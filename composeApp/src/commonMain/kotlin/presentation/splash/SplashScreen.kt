package presentation.splash

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.serialization.Serializable

@Serializable
data object SplashScreen : Screen

// UI State - simple, just shows loading
data object SplashUiState : CircuitUiState
