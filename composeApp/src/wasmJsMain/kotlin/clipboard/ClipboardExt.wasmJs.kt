package clipboard

import androidx.compose.ui.platform.ClipEntry

actual fun clipboardPlainText(text: String): ClipEntry {
    return ClipEntry.withPlainText(text)
}