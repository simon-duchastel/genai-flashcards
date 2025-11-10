package ai.solenne.flashcards.app.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Determine heading based on what's active
                val headingText = when {
                    state.logInState is LogInState.LoggedIn -> "Currently Using Solenne's AI"
                    state.apiKeyState is ApiKeyState.Loaded && (state.apiKeyState as ApiKeyState.Loaded).apiKey.isNotBlank() -> "Currently using my own AI"
                    else -> "Choose how your flashcards are generated"
                }

                Text(
                    text = headingText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
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
                        // Determine if each option is active
                        val isSolenneAiActive = state.logInState is LogInState.LoggedIn
                        val isOwnAiActive = state.apiKeyState is ApiKeyState.Loaded &&
                            (state.apiKeyState as ApiKeyState.Loaded).apiKey.isNotBlank()

                        // Option 1: Use Solenne's AI
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = if (isSolenneAiActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { state.onSolenneAiExpandedToggle() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSolenneAiActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                        )
                                    }
                                    Text(
                                        text = "Use Solenne's AI",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (state.solenneAiExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (state.solenneAiExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AnimatedVisibility(
                                visible = state.solenneAiExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(12.dp))

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

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Learn more link outside the border
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state.onSolenneAiInfoToggle() }
                                .padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Learn more",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider with "OR"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = " OR ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Option 2: Use my own AI
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = if (isOwnAiActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { state.onOwnAiExpandedToggle() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isOwnAiActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                        )
                                    }
                                    Text(
                                        text = "Use my own AI",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (state.ownAiExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (state.ownAiExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AnimatedVisibility(
                                visible = state.ownAiExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(12.dp))

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

                                        // Determine button text and action
                                        val hasOriginalKey = apiKeyState.originalApiKey.isNotBlank()
                                        val hasCurrentKey = apiKeyState.apiKey.isNotBlank()
                                        val keyChanged = apiKeyState.apiKey != apiKeyState.originalApiKey

                                        val (buttonText, buttonAction, buttonEnabled) = when {
                                            // Has original key and hasn't changed -> Remove
                                            hasOriginalKey && !keyChanged -> Triple("Remove", apiKeyState.onRemoveClicked, true)
                                            // Has original key and changed -> Update
                                            hasOriginalKey && keyChanged -> Triple("Update", apiKeyState.onSaveClicked, hasCurrentKey)
                                            // No original key and has text -> Add
                                            !hasOriginalKey && hasCurrentKey -> Triple("Add", apiKeyState.onSaveClicked, true)
                                            // No original key and no text -> disabled
                                            else -> Triple("Add", apiKeyState.onSaveClicked, false)
                                        }

                                        Button(
                                            onClick = buttonAction,
                                            enabled = buttonEnabled,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = if (buttonText == "Remove") {
                                                ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            } else {
                                                ButtonDefaults.buttonColors()
                                            }
                                        ) {
                                            Text(buttonText)
                                        }
                                    }
                                }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Learn more link outside the border
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { state.onOwnAiInfoToggle() }
                                .padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Learn more",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
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

                    Text(
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
                                Text(
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
                                Text(
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

    // Info dialog for "Use Solenne's AI"
    if (state.showSolenneAiInfo) {
        AlertDialog(
            onDismissRequest = state.onSolenneAiInfoToggle,
            title = { Text("Use Solenne's AI") },
            text = {
                Text(
                    text = "Sign in with Google or Apple to use Solenne's AI via the Solenne server. Your flashcards will be stored securely in your account.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = state.onSolenneAiInfoToggle) {
                    Text("Got it")
                }
            }
        )
    }

    // Info dialog for "Use my own AI"
    if (state.showOwnAiInfo) {
        AlertDialog(
            onDismissRequest = state.onOwnAiInfoToggle,
            title = { Text("Use my own AI") },
            text = {
                Column {
                    Text(
                        text = "Provide your own Gemini API key to generate flashcards using Google's AI directly. You never use Solenne's server and all of your data goes straight to Gemini's AI service. Your API key stays on your device.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val annotatedString = buildAnnotatedString {
                        withLink(LinkAnnotation.Url("https://aistudio.google.com/api-keys")) {
                            append("Get your API key from Google AI Studio")
                        }
                    }
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = state.onOwnAiInfoToggle) {
                    Text("Got it")
                }
            }
        )
    }
}