package ai.solenne.flashcards.app.presentation.auth

import ai.solenne.flashcards.app.presentation.components.SelectableText
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import genai_flashcards.composeapp.generated.resources.Res
import genai_flashcards.composeapp.generated.resources.apple_logo
import org.jetbrains.compose.resources.painterResource
import ai.solenne.flashcards.app.presentation.components.HelpText
import ai.solenne.flashcards.app.presentation.components.textWithHelpEmail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthUi(state: AuthUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { SelectableText("Settings", fontWeight = FontWeight.Bold) },
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
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                SelectableText(
                    text = when (state.logInState) {
                        is LogInState.LoggedIn -> "Signed in"
                        is LogInState.LoggedOut, is LogInState.Loading -> "Signed out"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (state.logInState) {
                        is LogInState.LoggedIn -> MaterialTheme.colorScheme.primary
                        is LogInState.LoggedOut, is LogInState.Loading -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectableText(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login/Logout section based on LogInState
                when (val loginState = state.logInState) {
                    is LogInState.LoggedIn -> {
                        Button(
                            onClick = loginState.onLogoutClicked,
                            enabled = state.deleteAccountModal !is DeleteAccountModal.Visible,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Logout")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    is LogInState.LoggedOut, is LogInState.Loading -> {
                        val onGoogleSignInClicked = when (loginState) {
                            is LogInState.LoggedOut -> loginState.onGoogleSignInClicked
                            is LogInState.Loading -> null
                            is LogInState.LoggedIn -> null
                        }
                        val googleButtonLoading = loginState is LogInState.Loading && loginState.loadingGoogle

                        Button(
                            onClick = onGoogleSignInClicked ?: {},
                            enabled = loginState is LogInState.LoggedOut && !googleButtonLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = loginState is LogInState.LoggedOut && !googleButtonLoading)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                if (googleButtonLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = "Sign in with Google",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val onAppleSignInClicked = when (loginState) {
                            is LogInState.LoggedOut -> loginState.onAppleSignInClicked
                            is LogInState.Loading -> null
                            is LogInState.LoggedIn -> null
                        }
                        val appleButtonLoading = loginState is LogInState.Loading && loginState.loadingApple
                        Button(
                            onClick = onAppleSignInClicked ?: {},
                            enabled = loginState is LogInState.LoggedOut && !appleButtonLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = loginState is LogInState.LoggedOut && !appleButtonLoading)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                if (appleButtonLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                Image(
                                    painter = painterResource(Res.drawable.apple_logo),
                                    contentDescription = "Apple logo",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Apple",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                    }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Divider with "OR"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    SelectableText(
                        text = " OR ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                SelectableText(
                    text = "Enter your Gemini API key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                ApiKeyLinkText()

                Spacer(modifier = Modifier.height(16.dp))

                when (val apiKeyState = state.apiKeyState) {
                    is ApiKeyState.Loading -> {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { CircularProgressIndicator() },
                            placeholder = { Text("AIza...") },
                            singleLine = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }
                    }
                    is ApiKeyState.Empty -> {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AIza...") },
                            singleLine = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }
                    }
                    is ApiKeyState.Loaded -> {
                        OutlinedTextField(
                            value = apiKeyState.apiKey,
                            onValueChange = apiKeyState.onApiKeyChanged,
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AIza...") },
                            singleLine = true,
                            enabled = true,
                            isError = state.error != null,
                            supportingText = state.error?.let { errorText ->
                                { Text(textWithHelpEmail(errorText, MaterialTheme.colorScheme.error), color = MaterialTheme.colorScheme.error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = apiKeyState.onSaveClicked,
                            enabled = apiKeyState.apiKey.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HelpText(modifier = Modifier.fillMaxWidth())

                // Dangerous actions section (only shown if logged in)
                if (state.logInState is LogInState.LoggedIn) {
                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))

                    SelectableText(
                        text = "Danger Zone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (val dangerousMode = state.dangerousModeState) {
                        is DangerousModeState.Disabled -> {
                            // Checkbox to enable dangerous actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dangerousMode.onDangerousModeToggled() }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = false,
                                    onCheckedChange = { dangerousMode.onDangerousModeToggled() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SelectableText(
                                    text = "I'd like to perform a dangerous action",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Delete account section - greyed out when dangerous mode not enabled
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(0.4f)
                            ) {
                                TextButton(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Account")
                                }
                            }
                        }
                        is DangerousModeState.Enabled -> {
                            // Checkbox to enable dangerous actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dangerousMode.onDangerousModeToggled() }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = { dangerousMode.onDangerousModeToggled() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SelectableText(
                                    text = "I'd like to perform a dangerous action",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Delete account section - enabled when dangerous mode is enabled
                            val isDeletingAccount = state.deleteAccountModal is DeleteAccountModal.Visible &&
                                (state.deleteAccountModal as? DeleteAccountModal.Visible)?.isDeletingAccount == true

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(1f)
                            ) {
                                TextButton(
                                    onClick = dangerousMode.onDeleteAccountClicked,
                                    enabled = !isDeletingAccount,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    if (isDeletingAccount) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.error,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete Account")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete account confirmation dialog
    when (val modal = state.deleteAccountModal) {
        is DeleteAccountModal.Visible -> {
            AlertDialog(
                onDismissRequest = modal.onDeleteAccountCancelled,
                title = { Text("Delete Account") },
                text = {
                    Text(
                        text = "Are you sure you want to delete your account? This action is IRREVERSIBLE and your flashcards will be gone FOREVER",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = modal.onDeleteAccountConfirmed,
                        enabled = !modal.isDeletingAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        if (modal.isDeletingAccount) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Yes, delete my account")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = modal.onDeleteAccountCancelled,
                        enabled = !modal.isDeletingAccount
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        is DeleteAccountModal.Hidden -> {
            // No dialog shown
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
