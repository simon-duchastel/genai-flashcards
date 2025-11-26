package ai.solenne.flashcards.app.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.foundation.layout.size
import org.jetbrains.compose.resources.painterResource
import genai_flashcards.composeapp.generated.resources.Res
import genai_flashcards.composeapp.generated.resources.dice_icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.app.presentation.components.textWithHelpEmail
import ai.solenne.flashcards.app.presentation.components.SelectableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUi(state: CreateUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    SelectableText(
                        if (state.isEditMode) "Edit Flashcards" else "Create Flashcards"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = state.onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
            val maxContentWidth = 840.dp
            val contentWidth = minOf(maxWidth, maxContentWidth)

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                when (val contentState = state.contentState) {
                    is ContentState.Idle -> CreateForm(contentState)
                    is ContentState.Generating -> GeneratingScreen(contentState)
                    is ContentState.Error -> ErrorForm(contentState)
                    is ContentState.Generated -> PreviewCards(contentState)
                }
            }
        }
    }

    when (val dialogState = state.deleteDialogState) {
        is DeleteDialogState.Hidden -> { /* No dialog */ }
        is DeleteDialogState.Visible -> {
            AlertDialog(
                onDismissRequest = dialogState.onCancel,
                title = { Text("Remove Flashcard") },
                text = { Text("Are you sure you want to remove this flashcard?") },
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

@Composable
private fun CreateForm(state: ContentState.Idle) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectableText(
            "What would you like to study?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.topic,
            onValueChange = state.onTopicChanged,
            label = { Text("Topic") },
            placeholder = { Text("ex.  \"Cats\", \"Sirens in Greek Mythology\", \"16th Century Witchcraft\"") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text(
                    "${state.topic.length}/30",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = state.onQueryChanged,
            label = { Text("Additional Details (Optional)") },
            placeholder = { Text("Describe what you want to focus on, any specific areas, difficulty level, etc.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        SelectableText(
            "How many flashcards?",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectableText("${state.count} cards")
            Slider(
                value = state.count.toFloat(),
                onValueChange = { state.onCountChanged(it.toInt()) },
                valueRange = 5f..50f,
                steps = 44,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = state.onGenerateClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.topic.isNotBlank()
        ) {
            Text("Generate Flashcards")
        }
    }
}

@Composable
private fun GeneratingScreen(state: ContentState.Generating) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        SelectableText(
            "Generating flashcards for \"${state.topic}\"...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorForm(state: ContentState.Error) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectableText(
            "What would you like to study?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.topic,
            onValueChange = state.onTopicChanged,
            label = { Text("Topic") },
            placeholder = { Text("ex.  \"Cats\", \"Sirens in Greek Mythology\", \"16th Century Witchcraft\"") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text(
                    "${state.topic.length}/30",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = state.onQueryChanged,
            label = { Text("Additional Details (Optional)") },
            placeholder = { Text("Describe what you want to focus on, any specific areas, difficulty level, etc.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        SelectableText(
            "How many flashcards?",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectableText("${state.count} cards")
            Slider(
                value = state.count.toFloat(),
                onValueChange = { state.onCountChanged(it.toInt()) },
                valueRange = 5f..50f,
                steps = 44,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            SelectableText(
                text = textWithHelpEmail(state.message, MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = state.onRetry,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.topic.isNotBlank()
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun PreviewCards(state: ContentState.Generated) {
    Column(modifier = Modifier.fillMaxSize()) {
        SelectableText(
            "Generated ${state.generatedCards.size} flashcards",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SelectableText(
            "Review and edit before saving",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Re-roll section
        when (val regenerationState = state.regenerationState) {
            is RegenerationState.Idle -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = regenerationState.regenerationPrompt,
                        onValueChange = regenerationState.onRegenerationPromptChanged,
                        placeholder = { Text("Not happy? Add instructions and re-roll (e.g., 'make them easier')") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = regenerationState.onRerollClicked
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.dice_icon),
                            contentDescription = "Re-roll flashcards"
                        )
                    }
                }
            }
            is RegenerationState.Regenerating -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = regenerationState.regenerationPrompt,
                        onValueChange = {},
                        placeholder = { Text("Not happy? Add instructions and re-roll (e.g., 'make them easier')") },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        singleLine = true
                    )
                    IconButton(
                        onClick = {},
                        enabled = false
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.generatedCards, key = { it.id }) { card ->
                FlashcardPreviewItem(
                    card = card,
                    onEdit = { front, back -> state.onEditCard(card.id, front, back) },
                    onDeleteClick = { state.onDeleteCardClick(card) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = state.onSaveClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isEditMode) "Update Set" else "Save Set")
        }
    }
}

@Composable
private fun FlashcardPreviewItem(
    card: Flashcard,
    onEdit: (String, String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                SelectableText(
                    "Front:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(24.dp).offset(y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
            SelectableText(
                text = card.front,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelectableText(
                "Back:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            SelectableText(
                text = card.back,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showEditDialog) {
        EditCardDialog(
            front = card.front,
            back = card.back,
            onDismiss = { showEditDialog = false },
            onSave = { newFront, newBack ->
                onEdit(newFront, newBack)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditCardDialog(
    front: String,
    back: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var editedFront by remember { mutableStateOf(front) }
    var editedBack by remember { mutableStateOf(back) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Flashcard") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = editedFront,
                    onValueChange = { editedFront = it },
                    label = { Text("Front") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = editedBack,
                    onValueChange = { editedBack = it },
                    label = { Text("Back") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editedFront, editedBack) },
                enabled = editedFront.isNotBlank() && editedBack.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
