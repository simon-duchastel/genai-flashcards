package ai.solenne.flashcards.app

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import genai_flashcards.composeapp.generated.resources.Res
import genai_flashcards.composeapp.generated.resources.mermaid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import ai.solenne.flashcards.app.presentation.splash.SplashScreen

data class ThemeState(
    val isDarkMode: Boolean,
    val onToggle: () -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No ThemeState provided")
}

val LocalSnackkbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun App(
    configRepository: ConfigRepository,
    circuit: Circuit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
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

    CompositionLocalProvider(
        LocalSnackkbarHostState provides snackbarHostState,
        LocalThemeState provides themeState,
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) {
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

                            BackHandler(enabled = backStack.size > 1) {
                                navigator.pop()
                            }

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
}

@Composable
fun FooterBanner() {
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
                text = buildAnnotatedString {
                    withLink(LinkAnnotation.Url("https://simon.duchastel.com")) {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            )
                        ) {
                            append("Simon Duchastel")
                        }
                    }
                },
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
