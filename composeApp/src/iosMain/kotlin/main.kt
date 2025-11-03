import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.window.ComposeUIViewController
import di.IosAppGraph
import di.createGraphFactory
import platform.UIKit.UIViewController

/**
 * Main entry point for iOS app.
 * Returns a UIViewController that contains the entire Compose UI.
 */
fun MainViewController(): UIViewController {
    // Create the DI graph using Metro
    val appGraph = createGraphFactory<IosAppGraph.Factory>().create()

    return ComposeUIViewController {
        SelectionContainer {
            App(
                configRepository = appGraph.configRepository,
                circuit = appGraph.circuit,
            )
        }
    }
}
