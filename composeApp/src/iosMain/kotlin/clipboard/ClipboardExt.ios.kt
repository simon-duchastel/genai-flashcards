package clipboard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun clipboardPlainText(text: String): ClipEntry {
    return ClipEntry.withPlainText(text)
}