package clipboard

import androidx.compose.ui.platform.ClipEntry

expect fun clipboardPlainText(text: String): ClipEntry
