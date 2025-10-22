package presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import presentation.components.HelpText
import presentation.components.textWithHelpEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthUi(state: AuthUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    if (state.onBackClicked != null) {
                        IconButton(onClick = state.onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val maxContentWidth = 600.dp
            val contentWidth = minOf(maxWidth, maxContentWidth)

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your Gemini API key to get started",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                ApiKeyLinkText()

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.apiKeyInput ?: "",
                    onValueChange = state.onApiKeyChanged,
                    label = {
                        if (state.apiKeyInput != null) {
                            Text("Gemini API Key")
                        } else {
                            CircularProgressIndicator()
                        }
                    },
                    placeholder = { Text("AIza...") },
                    singleLine = true,
                    enabled = !state.isSaving,
                    isError = state.error != null,
                    supportingText = state.error?.let { errorText ->
                        { Text(textWithHelpEmail(errorText, MaterialTheme.colorScheme.error), color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = state.onSaveClicked,
                    enabled = !state.isSaving && !state.apiKeyInput.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Continue")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HelpText(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ApiKeyLinkText() {
    val annotatedString = buildAnnotatedString {
        withLink(LinkAnnotation.Url("https://aistudio.google.com/api-keys")) {
            append("Get your API key from Google AI Studio")
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}
