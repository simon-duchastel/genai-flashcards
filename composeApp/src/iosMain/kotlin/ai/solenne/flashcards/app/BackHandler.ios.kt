package ai.solenne.flashcards.app

import androidx.compose.runtime.Composable

/**
 * iOS implementation.
 * iOS uses swipe gestures for navigation, so no explicit back button handling needed.
 */
@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // No-op - iOS uses swipe gestures, no back button to handle
}
