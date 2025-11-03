package ai.solenne.flashcards.app

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.window.ComposeUIViewController
import dev.zacsweers.metro.createGraph
import ai.solenne.flashcards.app.di.IosAppGraph
import platform.UIKit.UIViewController

/**
 * Main entry point for iOS app.
 * Returns a UIViewController that contains the entire Compose UI.
 */
fun MainViewController(): UIViewController {
    // Create the DI graph using Metro
    val appGraph = createGraph<IosAppGraph>()

    return ComposeUIViewController {
        SelectionContainer {
            App(
                configRepository = appGraph.configRepository,
                circuit = appGraph.circuit,
            )
        }
    }
}
