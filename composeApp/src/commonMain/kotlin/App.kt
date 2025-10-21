import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import data.ai.FlashcardGenerator
import data.storage.FlashcardStorage
import domain.repository.FlashcardRepository
import presentation.create.*
import presentation.home.*
import presentation.study.*

@Composable
fun App() {
    val storage = FlashcardStorage()
    val repository = FlashcardRepository(storage)
    val generator = FlashcardGenerator(apiKey = "") // TODO: Get from user input

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

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CircuitCompositionLocals(circuit) {
                val navigator = rememberCircuitNavigator(
                    initialScreen = HomeScreen(),
                    onRootPop = { /* Handle back on root */ }
                )

                NavigableCircuitContent(
                    navigator = navigator,
                    circuit = circuit
                )
            }
        }
    }
}
