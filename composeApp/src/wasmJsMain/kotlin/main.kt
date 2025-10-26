import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import data.api.ApiConfig
import data.api.AuthApiClient
import data.api.HttpClientProvider
import data.auth.GoogleOAuthHandler
import data.storage.getConfigRepository
import data.storage.getFlashcardStorage
import domain.generator.KoogFlashcardGenerator
import domain.repository.FlashcardRepository
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

/**
 * External interface for accessing URL search params.
 */
external interface URLSearchParams : JsAny {
    fun get(name: String): String?
}

/**
 * Create URLSearchParams from window.location.search
 */
@JsFun("() => new URLSearchParams(window.location.search)")
private external fun getURLSearchParams(): URLSearchParams

/**
 * Get query parameter value from URL.
 */
private fun getQueryParam(name: String): String? {
    return getURLSearchParams().get(name)
}

@OptIn(
    ExperimentalComposeUiApi::class,
    DelicateCoroutinesApi::class,
    ExperimentalWasmJsInterop::class,
)
fun main() {
    val configRepository = getConfigRepository()

    if (window.location.pathname == "/redirect") {
        // Extract token from query params
        val token = getQueryParam("token")

        if (token != null) {
            // Save session token to localStorage
            GlobalScope.launch {
                configRepository.setSessionToken(token)
                // Also extract and save user info if provided in query params
                val email = getQueryParam("email")
                val name = getQueryParam("name")
                val picture = getQueryParam("picture")

                email?.let { configRepository.setUserEmail(it) }
                name?.let { configRepository.setUserName(it) }
                picture?.let { configRepository.setUserPicture(it) }
            }

            // Clean up URL by replacing /redirect with / (no page reload!)
            window.history.replaceState(null, "", "/")
        }
    }

    // Normal app initialization
    val storage = getFlashcardStorage()
    val repository = FlashcardRepository(storage)
    val generator = KoogFlashcardGenerator(getGeminiApiKey =  configRepository::getGeminiApiKey )

    // Setup HTTP client and auth services
    val httpClient = HttpClientProvider.client
    val authApiClient = AuthApiClient(httpClient, ApiConfig.BASE_URL)
    val googleOAuthHandler = GoogleOAuthHandler(authApiClient)

    val circuit = Circuit.Builder()
        .addPresenterFactory { screen, navigator, _ ->
            when (screen) {
                is SplashScreen -> SplashPresenter(navigator, configRepository)
                is AuthScreen -> AuthPresenter(navigator, configRepository, googleOAuthHandler)
                is HomeScreen -> HomePresenter(navigator, repository)
                is CreateScreen -> CreatePresenter(screen, navigator, repository, generator)
                is StudyScreen -> StudyPresenter(screen, navigator, repository)
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

    ComposeViewport(
        content = {
            SelectionContainer {
                App(
                    configRepository = configRepository,
                    circuit = circuit,
                )
            }
        }
    )
}