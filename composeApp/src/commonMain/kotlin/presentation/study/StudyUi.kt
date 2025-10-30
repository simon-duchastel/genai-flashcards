package presentation.study

import LocalSnackkbarHostState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import clipboard.clipboardPlainText
import domain.model.Flashcard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
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
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
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

@OptIn(ExperimentalComposeUiApi::class)
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
    val clipboardManager = LocalClipboard.current
    val snackbarHostState = LocalSnackkbarHostState.current
    val scope = rememberCoroutineScope()

    val cardRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutCubic
        )
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
                    cameraDistance = 60f * density
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
                .combinedClickable(
                    onClick = onFlip,
                    onLongClick = {
                        val text = if (isFlipped) card.back else card.front
                        scope.launch {
                            clipboardManager.setClipEntry(clipboardPlainText(text))
                            snackbarHostState.showSnackbar("Text copied to clipboard")
                        }
                    }
                ),
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
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
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