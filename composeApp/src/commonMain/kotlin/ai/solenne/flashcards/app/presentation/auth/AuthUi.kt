package ai.solenne.flashcards.app.presentation.auth

import ai.solenne.flashcards.app.presentation.components.SelectableText
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthUi(state: AuthUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { SelectableText("Authentication", fontWeight = FontWeight.Bold) },
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
                // Determine heading based on login type
                val (headingText, subText) = when (val loginState = state.logInState) {
                    is LogInState.LoggedIn -> when (loginState.loginType) {
                        LogInState.LoggedIn.LoginType.SignedInWithGoogle -> "Signed in with Google" to "(using Solenne's AI)"
                        LogInState.LoggedIn.LoginType.SignedInWithApple -> "Signed in with Apple" to "(using Solenne's AI)"
                    }
                    else -> when  {
                        state.apiKeyState.currentlyUsingApiKeyOrNull == true -> "Currently using your own API key" to "(not signed in)"
                        else -> "Choose how your flashcards are generated" to null
                    }
                }

                SelectableText(
                    text = headingText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (subText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectableText(
                        text = subText,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isSolenneAiActive = state.logInState is LogInState.LoggedIn
                val isOwnAiActive = state.apiKeyState.currentlyUsingApiKeyOrNull == true

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

                            val loginState = state.logInState
                            val onGoogleSignInClicked = when (loginState) {
                                is LogInState.LoggedOut -> loginState.onGoogleSignInClicked
                                is LogInState.Loading -> null
                                is LogInState.LoggedIn -> null
                            }
                            val googleButtonLoading = loginState is LogInState.Loading && loginState.loadingGoogle
                            val isSignedInWithGoogle = loginState is LogInState.LoggedIn &&
                                loginState.loginType == LogInState.LoggedIn.LoginType.SignedInWithGoogle

                            Button(
                                onClick = onGoogleSignInClicked ?: {},
                                enabled = loginState is LogInState.LoggedOut && !googleButtonLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (isSignedInWithGoogle) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF4CAF50),
                                        disabledContentColor = Color.White
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                border = if (!isSignedInWithGoogle) {
                                    ButtonDefaults.outlinedButtonBorder(enabled = loginState is LogInState.LoggedOut && !googleButtonLoading)
                                } else {
                                    null
                                }
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
                                        if (isSignedInWithGoogle) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Signed in",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                            )
                                        }
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        Text(
                                            text = if (isSignedInWithGoogle) "Signed in with Google" else "Sign in with Google",
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
                            val isSignedInWithApple = loginState is LogInState.LoggedIn &&
                                loginState.loginType == LogInState.LoggedIn.LoginType.SignedInWithApple

                            Button(
                                onClick = onAppleSignInClicked ?: {},
                                enabled = loginState is LogInState.LoggedOut && !appleButtonLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (isSignedInWithApple) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF4CAF50),
                                        disabledContentColor = Color.White
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                border = if (!isSignedInWithApple) {
                                    ButtonDefaults.outlinedButtonBorder(enabled = loginState is LogInState.LoggedOut && !appleButtonLoading)
                                } else {
                                    null
                                }
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
                                        if (isSignedInWithApple) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Signed in",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                            )
                                        }
                                        Image(
                                            painter = painterResource(Res.drawable.apple_logo),
                                            contentDescription = "Apple logo",
                                            modifier = Modifier.size(20.dp),
                                            colorFilter = ColorFilter.tint(if (isSignedInWithApple) Color.White else MaterialTheme.colorScheme.onSurface)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (isSignedInWithApple) "Signed in with Apple" else "Sign in with Apple",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Logout button (shown when logged in or logging out)
                            val showLogoutButton = loginState is LogInState.LoggedIn ||
                                (loginState is LogInState.Loading && loginState.loadingLogout)
                            if (showLogoutButton) {
                                Spacer(modifier = Modifier.height(12.dp))

                                val logoutButtonLoading = loginState is LogInState.Loading && loginState.loadingLogout
                                val onLogoutClicked = (loginState as? LogInState.LoggedIn)?.onLogoutClicked ?: {}
                                Button(
                                    onClick = onLogoutClicked,
                                    enabled = state.deleteAccountModal !is DeleteAccountModal.Visible && !logoutButtonLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    if (logoutButtonLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else {
                                        Text(
                                            text = "Logout",
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
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
                                }

                                is ApiKeyState.Loaded, is ApiKeyState.Modified -> {
                                    val apiKey = when (apiKeyState) {
                                        is ApiKeyState.Loaded -> apiKeyState.apiKey
                                        is ApiKeyState.Modified -> apiKeyState.apiKey
                                        else -> "" // this should never happen
                                    }
                                    val onApiKeyChanged = when (apiKeyState) {
                                        is ApiKeyState.Loaded -> apiKeyState.onApiKeyChanged
                                        is ApiKeyState.Modified -> apiKeyState.onApiKeyChanged
                                        else -> ({}) // this should never happen
                                    }
                                    val onRemoveClicked = when (apiKeyState) {
                                        is ApiKeyState.Loaded -> apiKeyState.onRemoveClicked
                                        is ApiKeyState.Modified -> apiKeyState.onRemoveClicked
                                        else -> ({}) // this should never happen
                                    }

                                    ApiKeyTextField(
                                        apiKey = apiKey,
                                        onApiKeyChanged = onApiKeyChanged,
                                        error = state.error
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // if the api key isn't modified, it's disabled
                                    val onClick = (apiKeyState as? ApiKeyState.Modified)?.onButtonClicked
                                    val currentlyUsingApiKey = apiKeyState.currentlyUsingApiKeyOrNull ?: false
                                    val apiKeyModifiedAndNowEmpty = (apiKeyState as? ApiKeyState.Modified)?.apiKey?.isBlank() ?: false
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val showAddUpdateButton = !apiKeyModifiedAndNowEmpty
                                        val showRemoveButton = currentlyUsingApiKey

                                        val addUpdateWeight by animateFloatAsState(
                                            targetValue = if (showAddUpdateButton) 1f else 0f,
                                            animationSpec = tween(durationMillis = 300),
                                            label = "AddUpdateWeight"
                                        )
                                        val removeWeight by animateFloatAsState(
                                            targetValue = if (showRemoveButton) 1f else 0f,
                                            animationSpec = tween(durationMillis = 300),
                                            label = "RemoveWeight"
                                        )
                                        val spacerWidth by animateDpAsState(
                                            targetValue = if (showAddUpdateButton && showRemoveButton) 8.dp else 0.dp,
                                            animationSpec = tween(durationMillis = 300),
                                            label = "SpacerWidth"
                                        )

                                        if (addUpdateWeight > 0f) {
                                            Button(
                                                onClick = onClick ?: {},
                                                enabled = onClick != null,
                                                modifier = Modifier.weight(addUpdateWeight),
                                                colors = ButtonDefaults.buttonColors()
                                            ) {
                                                Text(
                                                    text = if (currentlyUsingApiKey) {
                                                        "Update"
                                                    } else {
                                                        "Add"
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Clip,
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(spacerWidth))

                                        if (removeWeight > 0f) {
                                            Button(
                                                onClick = onRemoveClicked,
                                                enabled = true,
                                                modifier = Modifier.weight(removeWeight),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            ) {
                                                Text(
                                                    text = "Remove",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Clip,
                                                )
                                            }
                                        }
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
                    val isDeletingAccount = state.deleteAccountModal is DeleteAccountModal.Visible && state.deleteAccountModal.isDeletingAccount

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

@Composable
private fun ApiKeyTextField(
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = apiKey,
        onValueChange = onApiKeyChanged,
        label = { Text("Gemini API Key") },
        placeholder = { Text("AIza...") },
        singleLine = true,
        enabled = true,
        isError = error != null,
        supportingText = error?.let { errorText ->
            { Text(textWithHelpEmail(errorText, MaterialTheme.colorScheme.error), color = MaterialTheme.colorScheme.error) }
        },
        modifier = modifier.fillMaxWidth()
    )
}