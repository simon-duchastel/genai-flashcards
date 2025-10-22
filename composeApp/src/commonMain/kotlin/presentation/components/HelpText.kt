package presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

@Composable
fun HelpText(
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    val annotatedString = textWithHelpEmail("Questions? Feedback? Email help@solenne.ai")
    Text(
        modifier = modifier,
        text = annotatedString,
        textAlign = textAlign
    )
}

/**
 * Helper function that converts text containing "help@solenne.ai" into an AnnotatedString
 * with a clickable mailto link.
 */
@Composable
fun textWithHelpEmail(text: String, linkColor: Color = MaterialTheme.colorScheme.primary): AnnotatedString {
    val parts = text.split("help@solenne.ai")

    return buildAnnotatedString {
        if (parts.size == 1) {
            // No email found, return plain text
            append(text)
        } else {
            // Add parts with linked email in between
            parts.forEachIndexed { index, part ->
                append(part)
                if (index < parts.size - 1) {
                    withLink(LinkAnnotation.Url("mailto:help@solenne.ai")) {
                        withStyle(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("help@solenne.ai")
                        }
                    }
                }
            }
        }
    }
}
