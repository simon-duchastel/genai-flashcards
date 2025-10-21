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
            val apiKey = configRepository.getGeminiApiKey()
            if (apiKey != null) {
                navigator.resetRoot(HomeScreen)
            } else {
                navigator.resetRoot(AuthScreen)
            }
        }

        return SplashUiState
    }
}
