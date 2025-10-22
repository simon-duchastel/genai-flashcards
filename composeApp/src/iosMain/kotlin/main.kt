import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.window.ComposeUIViewController
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import data.storage.getConfigRepository
import data.storage.getFlashcardStorage
import domain.generator.KoogFlashcardGenerator
import domain.repository.FlashcardRepository
import platform.UIKit.UIViewController
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
 * Main entry point for iOS app.
 * Returns a UIViewController that contains the entire Compose UI.
 */
fun MainViewController(): UIViewController {
    val storage = getFlashcardStorage()
    val repository = FlashcardRepository(storage)
    val configRepository = getConfigRepository()
    val generator = KoogFlashcardGenerator(getGeminiApiKey = configRepository::getGeminiApiKey)

    val circuit = Circuit.Builder()
        .addPresenterFactory { screen, navigator, _ ->
            when (screen) {
                is SplashScreen -> SplashPresenter(navigator, configRepository)
                is AuthScreen -> AuthPresenter(navigator, configRepository)
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

    return ComposeUIViewController {
        SelectionContainer {
            App(
                configRepository = configRepository,
                circuit = circuit,
            )
        }
    }
}
