package ai.solenne.flashcards.app.presentation.home

import ai.solenne.flashcards.app.LocalThemeState
import ai.solenne.flashcards.app.presentation.components.SelectableText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.solenne.flashcards.app.domain.model.FlashcardSetWithMeta
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {
    val themeState = LocalThemeState.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { SelectableText("Solenne Flashcards", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = themeState.onToggle) {
                        Icon(
                            imageVector = if (themeState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }
                    IconButton(onClick = state.onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = state.onCreateNewSet,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        when (val contentState = state.contentState) {
            is ContentState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ContentState.Error -> {
                ErrorState(
                    message = contentState.message,
                    onRetry = contentState.onRetry,
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
            is ContentState.Loaded -> {
                if (contentState.flashcardSets.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        onCreateClick = state.onCreateNewSet
                    )
                } else {
                    FlashcardSetList(
                        sets = contentState.flashcardSets,
                        onSetClick = { contentState.onOpenSet(it.flashcardSet.id) },
                        onDeleteClick = contentState.onDeleteSetClick,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
            }
        }
    }

    when (val dialogState = state.deleteDialogState) {
        is DeleteDialogState.Hidden -> { /* No dialog */ }
        is DeleteDialogState.Visible -> {
            DisableSelection {
                AlertDialog(
                    onDismissRequest = dialogState.onCancel,
                    title = { Text("Remove Set") },
                    text = { Text("Are you sure you want to remove \"${dialogState.set.flashcardSet.topic}\"?") },
                    confirmButton = {
                        Button(onClick = dialogState.onConfirm) {
                            Text("Remove")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = dialogState.onCancel) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SelectableText(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SelectableText(
            "No flashcards yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        SelectableText(
            "Create your first set to get started!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Text("Create New Set")
        }
    }
}

@Composable
private fun FlashcardSetList(
    sets: List<FlashcardSetWithMeta>,
    onSetClick: (FlashcardSetWithMeta) -> Unit,
    onDeleteClick: (FlashcardSetWithMeta) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val maxContentWidth = 840.dp
        val contentWidth = minOf(maxWidth, maxContentWidth)

        LazyColumn(
            modifier = Modifier
                .width(contentWidth)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sets, key = { it.flashcardSet.id }) { setWithMeta ->
                FlashcardSetItem(
                    setWithMeta = setWithMeta,
                    onClick = { onSetClick(setWithMeta) },
                    onDeleteClick = { onDeleteClick(setWithMeta) }
                )
            }
        }
    }
}

@Composable
private fun FlashcardSetItem(
    setWithMeta: FlashcardSetWithMeta,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val set = setWithMeta.flashcardSet

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableText(
                        text = set.topic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (setWithMeta.isLocalOnly) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "Local Only",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                SelectableText(
                    text = "${set.cardCount} cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SelectableText(
                    text = formatDate(set.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatDate(timestamp: Long): String {
//    val instant = Instant.fromEpochMilliseconds(timestamp)
//    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
//    val date = dateTime.date
//    return "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
    return "${timestamp}"
}
