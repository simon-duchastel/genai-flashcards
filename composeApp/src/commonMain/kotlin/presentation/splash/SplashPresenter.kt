package presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import data.storage.ConfigRepository
import presentation.auth.AuthScreen
import presentation.home.HomeScreen

class SplashPresenter(
    private val navigator: Navigator,
    private val configRepository: ConfigRepository
) : Presenter<SplashUiState> {

    @Composable
    override fun present(): SplashUiState {
        LaunchedEffect(Unit) {
            // Check for session token first (OAuth authentication)
            val sessionToken = configRepository.getSessionToken()

            if (sessionToken != null) {
                // User is authenticated via Google OAuth
                navigator.resetRoot(HomeScreen)
            } else {
                // Fall back to API key check (for backwards compatibility)
                val apiKey = configRepository.getGeminiApiKey()
                if (apiKey != null) {
                    navigator.resetRoot(HomeScreen)
                } else {
                    // No authentication found, show auth screen
                    navigator.resetRoot(AuthScreen)
                }
            }
        }

        return SplashUiState
    }
}
