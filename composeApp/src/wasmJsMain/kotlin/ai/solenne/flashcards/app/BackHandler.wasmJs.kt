package ai.solenne.flashcards.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * wasmJs implementation with browser history integration.
 *
 * This implementation hooks into the browser's history API to ensure that:
 * - When the user clicks the browser back button, the onBack callback is triggered
 * - The browser history stays in sync with the app's navigation stack
 */
@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val currentEnabled by rememberUpdatedState(enabled)

    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }

        window.history.pushState(null, "", window.location.href)

        val handlePopState: (Event) -> Unit = {
            if (currentEnabled) {
                currentOnBack()
                window.history.pushState(null, "", window.location.href)
            }
        }

        window.addEventListener("popstate", handlePopState)
        onDispose {
            window.removeEventListener("popstate", handlePopState)
        }
    }
}
