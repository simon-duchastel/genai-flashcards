package ai.solenne.flashcards.app

import androidx.compose.runtime.Composable

/**
 * Cross-platform back handler for navigation.
 *
 * @param enabled Whether the back handler is enabled
 * @param onBack Callback to invoke when back is pressed
 */
@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)
