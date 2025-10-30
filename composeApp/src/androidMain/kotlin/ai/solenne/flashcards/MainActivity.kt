package ai.solenne.flashcards

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.text.selection.SelectionContainer
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import data.api.ApiConfig
import data.api.AuthApiClient
import data.api.HttpClientProvider
import data.api.ServerFlashcardApiClient
import data.api.ServerFlashcardGenerator
import data.auth.ChromeCustomTabsOAuthHandler
import data.repository.AuthRepositoryImpl
import data.storage.getConfigRepository
import data.storage.getFlashcardStorage
import data.storage.initConfigRepository
import data.storage.initFlashcardStorage
import domain.generator.KoogFlashcardGenerator
import domain.repository.ClientFlashcardRepository
import domain.repository.LocalFlashcardRepository
import presentation.auth.AuthPresenter
import presentation.auth.AuthScreen
import presentation.auth.AuthUi
import presentation.auth.AuthUiState
import presentation.create.CreatePresenter
import presentation.create.CreateScreen
import presentation.create.CreateUi
import presentation.create.CreateUiState
import presentation.home.HomePresenter
import presentation.home.HomeScreen
import presentation.home.HomeUi
import presentation.home.HomeUiState
import presentation.splash.SplashPresenter
import presentation.splash.SplashScreen
import presentation.splash.SplashUi
import presentation.splash.SplashUiState
import presentation.study.StudyPresenter
import presentation.study.StudyScreen
import presentation.study.StudyUi
import presentation.study.StudyUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle OAuth callback if present
        handleOAuthCallbackIfNeeded(intent)

        // Initialize storage repositories
        initFlashcardStorage(applicationContext)
        initConfigRepository(applicationContext)

        val storage = getFlashcardStorage()
        val configRepository = getConfigRepository()
        val httpClient = HttpClientProvider.client
        val authRepository = AuthRepositoryImpl(configRepository)
        val serverFlashcardClient = ServerFlashcardApiClient(httpClient, ApiConfig.BASE_URL)
        val serverGenerator = ServerFlashcardGenerator(httpClient, ApiConfig.BASE_URL, configRepository)
        val koogGenerator = KoogFlashcardGenerator(getGeminiApiKey = configRepository::getGeminiApiKey)

        val clientRepository = ClientFlashcardRepository(
            authRepository = authRepository,
            serverClient = serverFlashcardClient
        )
        val localRepository = LocalFlashcardRepository(
            storage = storage
        )

        val authApiClient = AuthApiClient(
            isTest = false,
            httpClient = httpClient,
            baseUrl = ApiConfig.BASE_URL
        )
        val oauthHandler = data.auth.getOAuthHandler(applicationContext, authApiClient)

        val circuit = Circuit.Builder()
            .addPresenterFactory { screen, navigator, _ ->
                when (screen) {
                    is SplashScreen -> SplashPresenter(navigator, configRepository)
                    is AuthScreen -> AuthPresenter(navigator, configRepository, oauthHandler, authApiClient)
                    is HomeScreen -> HomePresenter(navigator, authRepository, clientRepository, localRepository)
                    is CreateScreen -> CreatePresenter(screen, navigator, authRepository, clientRepository, localRepository, serverGenerator, koogGenerator)
                    is StudyScreen -> StudyPresenter(screen, navigator, authRepository, clientRepository, localRepository)
                    else -> null
                }
            }
            .addUiFactory { screen, _ ->
                when (screen) {
                    is SplashScreen -> ui<SplashUiState> { state, modifier ->
                        SplashUi(state, modifier)
                    }
                    is AuthScreen -> ui<AuthUiState> { state, modifier ->
                        AuthUi(state, modifier)
                    }
                    is HomeScreen -> ui<HomeUiState> { state, modifier ->
                        HomeUi(state, modifier)
                    }
                    is CreateScreen -> ui<CreateUiState> { state, modifier ->
                        CreateUi(state, modifier)
                    }
                    is StudyScreen -> ui<StudyUiState> { state, modifier ->
                        StudyUi(state, modifier)
                    }
                    else -> null
                }
            }
            .build()

        setContent {
            SelectionContainer {
                App(
                    configRepository = configRepository,
                    circuit = circuit,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallbackIfNeeded(intent)
    }

    private fun handleOAuthCallbackIfNeeded(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "solenne-flashcards" && uri.host == "callback") {
                ChromeCustomTabsOAuthHandler.handleOAuthCallback(uri.toString())
            }
        }
    }
}
