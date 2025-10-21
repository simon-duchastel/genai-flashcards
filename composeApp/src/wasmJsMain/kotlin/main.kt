import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import data.ai.FlashcardGenerator
import data.storage.getFlashcardStorage
import domain.repository.FlashcardRepository
import presentation.create.CreatePresenter
import presentation.create.CreateScreen
import presentation.create.CreateUi
import presentation.create.CreateUiState
import presentation.home.HomePresenter
import presentation.home.HomeScreen
import presentation.home.HomeUi
import presentation.home.HomeUiState
import presentation.study.StudyPresenter
import presentation.study.StudyScreen
import presentation.study.StudyUi
import presentation.study.StudyUiState

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val storage = getFlashcardStorage()
    val repository = FlashcardRepository(storage)
    val generator = FlashcardGenerator(apiKey = "")

    val circuit = Circuit.Builder()
        .addPresenterFactory { screen, navigator, _ ->
            when (screen) {
                is HomeScreen -> HomePresenter(navigator, repository)
                is CreateScreen -> CreatePresenter(screen, navigator, repository, generator)
                is StudyScreen -> StudyPresenter(screen, navigator, repository)
                else -> null
            }
        }
        .addUiFactory { screen, _ ->
            when (screen) {
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
            App(
                storage = storage,
                repository = repository,
                generator = generator,
                circuit = circuit,
            )
        }
    )
}