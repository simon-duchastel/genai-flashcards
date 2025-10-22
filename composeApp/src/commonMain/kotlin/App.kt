import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import genai_flashcards.composeapp.generated.resources.Res
import genai_flashcards.composeapp.generated.resources.mermaid
import org.jetbrains.compose.resources.painterResource
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.LocalCircuit
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import data.ai.FlashcardGenerator
import data.storage.ConfigRepository
import data.storage.FlashcardStorage
import data.storage.getFlashcardStorage
import domain.repository.FlashcardRepository
import presentation.create.*
import presentation.home.*
import presentation.splash.*
import presentation.study.*

@Composable
fun App(
    circuit: Circuit,
) {
    MaterialTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        CompositionLocalProvider(LocalSnackkbarHostState provides snackbarHostState) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CircuitCompositionLocals(circuit) {
                        val backStack = rememberSaveableBackStack(SplashScreen)
                        val navigator = rememberCircuitNavigator(
                            backStack = backStack,
                            onRootPop = { }, // no-op root pop
                        )

                        NavigableCircuitContent(
                            navigator = navigator,
                            backStack = backStack,
                            circuit = circuit,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                    FooterBanner()
                }
            }
        }
    }
}

val LocalSnackkbarHostState = staticCompositionLocalOf<SnackbarHostState?> { null }

@Composable
fun FooterBanner() {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color =   MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "built with ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Image(
                painter = painterResource(Res.drawable.mermaid),
                contentDescription = "Mermaid",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = " by ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Simon Duchastel",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://simon.duchastel.com")
                }
            )
        }
    }
}