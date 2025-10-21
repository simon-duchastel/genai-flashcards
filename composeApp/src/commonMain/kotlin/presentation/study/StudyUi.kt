package presentation.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyUi(state: StudyUiState, modifier: Modifier = Modifier) {
    if (state.flashcards.isEmpty()) {
        LoadingState(modifier)
        return
    }

    if (state.isComplete) {
        CompletionScreen(
            state = state,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.topic) },
                navigationIcon = {
                    IconButton(onClick = state.onExitStudy) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    Text(
                        state.progress,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            state.currentCard?.let { card ->
                FlashcardView(
                    card = card,
                    isFlipped = state.isFlipped,
                    onFlip = state.onFlipCard,
                    onSwipeLeft = state.onPreviousCard,
                    onSwipeRight = state.onNextCard,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            NavigationButtons(
                canGoPrevious = state.currentIndex > 0,
                canGoNext = state.currentIndex < state.flashcards.size - 1,
                onPrevious = state.onPreviousCard,
                onNext = state.onNextCard,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FlashcardView(
    card: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableStateOf(0f) }

    val cardRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f
    )

    BoxWithConstraints(modifier = modifier) {
        val maxCardWidth = 700.dp
        val cardWidth = minOf(maxWidth, maxCardWidth)

        Card(
            modifier = Modifier
                .width(cardWidth)
                .align(Alignment.Center)
                .heightIn(min = 280.dp, max = maxHeight)
                .graphicsLayer {
                    rotationY = cardRotation
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                swipeOffset > 100 -> onSwipeRight()
                                swipeOffset < -100 -> onSwipeLeft()
                            }
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffset += dragAmount
                        }
                    )
                }
                .clickable(onClick = onFlip),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = isFlipped) { flipped ->
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val text = if (flipped) card.back else card.front
                        val textLength = text.length
                        val fontSize = when {
                            textLength > 300 -> MaterialTheme.typography.bodyLarge
                            textLength > 150 -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.headlineSmall
                        }

                        Text(
                            text = text,
                            style = fontSize,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    rotationY = if (flipped) 180f else 0f
                                }
                        )
                    }
                }
            }
        }
    }

    // Hint text
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = if (isFlipped) "Swipe or tap to flip" else "Tap to reveal answer",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NavigationButtons(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoPrevious,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Previous")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f)
        ) {
            Text("Next")
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletionScreen(
    state: StudyUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Study Complete!") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You've completed all flashcards!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Studied ${state.flashcards.size} cards on ${state.topic}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = state.onExitStudy) {
                    Text("Go Home")
                }
                Button(onClick = state.onRestartStudy) {
                    Text("Study Again")
                }
            }
        }
    }
}