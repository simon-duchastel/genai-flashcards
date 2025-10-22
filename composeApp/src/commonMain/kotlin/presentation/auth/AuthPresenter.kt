package presentation.auth

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import data.storage.ConfigRepository
import kotlinx.coroutines.launch
import presentation.home.HomeScreen
import presentation.splash.SplashScreen

class AuthPresenter(
    private val navigator: Navigator,
    private val configRepository: ConfigRepository
) : Presenter<AuthUiState> {

    @Composable
    override fun present(): AuthUiState {
        val canGoBack = remember { navigator.peekBackStack().size > 1 }
        var apiKeyInput: String? by remember { mutableStateOf(null) }
        var isSaving by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            val currentApiKey = configRepository.getGeminiApiKey()
            if (currentApiKey != null) {
                apiKeyInput = currentApiKey
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
            }
        )
    }
}
