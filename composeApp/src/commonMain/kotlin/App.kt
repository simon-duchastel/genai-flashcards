import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.rememberSaveableBackStack
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
import data.storage.getFlashcardStorage
import domain.repository.FlashcardRepository
import presentation.create.*
import presentation.home.*
import presentation.study.*

@Composable
fun App(
    storage: FlashcardStorage,
    repository: FlashcardRepository,
    generator: FlashcardGenerator,
    circuit: Circuit,
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CircuitCompositionLocals(circuit) {
                val backStack = rememberSaveableBackStack(HomeScreen)
                val navigator = rememberCircuitNavigator(
                    backStack = backStack,
                    onRootPop = { }, // no-op root pop
                )

                NavigableCircuitContent(
                    navigator = navigator,
                    backStack = backStack,
                    circuit = circuit,
                )
            }
        }
    }
}
