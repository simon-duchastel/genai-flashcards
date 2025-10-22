import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import data.storage.getConfigRepository
import genai_flashcards.composeapp.generated.resources.Res
import genai_flashcards.composeapp.generated.resources.mermaid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import presentation.splash.SplashScreen

data class ThemeState(
    val isDarkMode: Boolean,
    val onToggle: () -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No ThemeState provided")
}

@Composable
fun App(
    circuit: Circuit,
) {
    val configRepository = remember { getConfigRepository() }
    val coroutineScope = rememberCoroutineScope()
    var isDarkMode by remember { mutableStateOf(false) }

    // Load theme preference
    LaunchedEffect(Unit) {
        isDarkMode = configRepository.isDarkMode()
    }

    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    val themeState = ThemeState(
        isDarkMode = isDarkMode,
        onToggle = {
            coroutineScope.launch {
                isDarkMode = !isDarkMode
                configRepository.setDarkMode(isDarkMode)
            }
        }
    )

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalThemeState provides themeState) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
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

                    // Footer banner
                    val uriHandler = LocalUriHandler.current
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer,
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
            }
        }
    }
}