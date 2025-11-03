package ai.solenne.flashcards

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.text.selection.SelectionContainer
import data.auth.ChromeCustomTabsOAuthHandler
import dev.zacsweers.metro.createGraphFactory
import di.AndroidAppGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appGraph = createGraphFactory<AndroidAppGraph.Factory>().create(applicationContext)

        setContent {
            SelectionContainer {
                App(
                    configRepository = appGraph.configRepository,
                    circuit = appGraph.circuit,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallbackIfNeeded(intent)
    }

    private fun handleOAuthCallbackIfNeeded(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "solenne-flashcards" && uri.host == "callback") {
                ChromeCustomTabsOAuthHandler.handleOAuthCallback(uri.toString())
            }
        }
    }
}
