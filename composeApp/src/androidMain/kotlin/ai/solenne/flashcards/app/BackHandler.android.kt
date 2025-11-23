package ai.solenne.flashcards.app

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as AndroidBackHandler

/**
 * Android implementation using androidx.activity.compose.BackHandler
 */
@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    AndroidBackHandler(enabled = enabled, onBack = onBack)
}
