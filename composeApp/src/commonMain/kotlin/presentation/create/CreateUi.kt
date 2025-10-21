package presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUi(state: CreateUiState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Create Flashcards") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.generatedCards.isEmpty()) {
                CreateForm(state)
            } else {
                PreviewCards(state)
            }
        }
    }
}

@Composable
private fun CreateForm(state: CreateUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "What would you like to study?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.topic,
            onValueChange = state.onTopicChanged,
            label = { Text("Topic") },
            placeholder = { Text("e.g., Kotlin Coroutines") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isGenerating,
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
            enabled = !state.isGenerating,
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        Text(
            "How many flashcards?",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${state.count} cards")
            Slider(
                value = state.count.toFloat(),
                onValueChange = { state.onCountChanged(it.toInt()) },
                valueRange = 5f..50f,
                steps = 44,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                enabled = !state.isGenerating
            )
        }

        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = state.error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = state.onGenerateClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isGenerating && state.topic.isNotBlank()
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating...")
            } else {
                Text("Generate Flashcards")
            }
        }
    }
}

@Composable
private fun PreviewCards(state: CreateUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Generated ${state.generatedCards.size} flashcards",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Review and edit before saving",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.generatedCards, key = { it.id }) { card ->
                FlashcardPreviewItem(
                    card = card,
                    onDelete = { state.onDeleteCard(card.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = state.onBackClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = state.onSaveClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Set")
            }
        }
    }
}

@Composable
private fun FlashcardPreviewItem(
    card: Flashcard,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text(
                    "Front:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
            Text(
                text = card.front,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Back:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = card.back,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
