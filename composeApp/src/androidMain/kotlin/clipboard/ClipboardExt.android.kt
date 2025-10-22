package clipboard

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun clipboardPlainText(text: String): ClipEntry {
    return ClipEntry(ClipData.newPlainText("text", text))
}