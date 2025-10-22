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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import domain.model.Flashcard
import kotlinx.coroutines.launch

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
                    currentIndex = state.currentIndex,
                    totalCards = state.flashcards.size,
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
    currentIndex: Int,
    totalCards: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit, // This is now "onPrevious"
    onSwipeRight: () -> Unit, // This is now "onNext"
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var targetSwipeOffset by remember { mutableStateOf(0f) }
    var displayedCard by remember { mutableStateOf(card) }
    var displayedIsFlipped by remember { mutableStateOf(isFlipped) }
    var previousIndex by remember { mutableStateOf(currentIndex) }
    var isExiting by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val clipboardManager = LocalClipboard.current
    val snackbarHostState = LocalSnackkbarHostState.current
    val scope = rememberCoroutineScope()

    val isGoingNext = currentIndex > previousIndex
    val isGoingPrevious = currentIndex < previousIndex

    // Detect when index changes and trigger enter/exit animations
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        if (currentIndex != previousIndex) {
            val exitTo = if (isGoingNext) 1500f else -1500f
            val enterFrom = if (isGoingNext) 1500f else -1500f

            if (!isExiting) {
                // Button click: animate out the old card, then animate in the new one
                targetSwipeOffset = exitTo
                kotlinx.coroutines.delay(50) // Allow exit animation to start
                displayedCard = card
                displayedIsFlipped = isFlipped
                targetSwipeOffset = enterFrom // Position new card off-screen
                kotlinx.coroutines.delay(50)
                targetSwipeOffset = 0f // Animate new card in
            } else {
                // Swipe: old card has already exited, just animate the new one in
                targetSwipeOffset = enterFrom
                displayedCard = card
                displayedIsFlipped = isFlipped
                kotlinx.coroutines.delay(50)
                targetSwipeOffset = 0f
                isExiting = false
            }
            previousIndex = currentIndex
        }
    }

    val animatedSwipeOffset by animateFloatAsState(
        targetValue = targetSwipeOffset,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutCubic
        ),
        finishedListener = {
            swipeOffset = 0f
            // If there's a pending action (from swipe exit), execute it now
            if (pendingAction != null) {
                val action = pendingAction
                pendingAction = null
                action?.invoke()
            }
        }
    )

    val cardRotation by animateFloatAsState(
        targetValue = if (displayedIsFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutCubic
        )
    )

    val canGoNext = currentIndex < totalCards - 1
    val canGoPrevious = currentIndex > 0

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
                    translationX = animatedSwipeOffset
                    rotationZ = animatedSwipeOffset / 40f
                    alpha = 1f - (kotlin.math.abs(animatedSwipeOffset) / 1500f)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                // Swipe Right -> Next Card
                                swipeOffset > 100 && canGoNext -> {
                                    isExiting = true
                                    targetSwipeOffset = 1500f // Animate out to the right
                                    pendingAction = onSwipeRight
                                }
                                // Swipe Left -> Previous Card
                                swipeOffset < -100 && canGoPrevious -> {
                                    isExiting = true
                                    targetSwipeOffset = -1500f // Animate out to the left
                                    pendingAction = onSwipeLeft
                                }
                                else -> {
                                    // Not enough swipe, spring back
                                    targetSwipeOffset = 0f
                                }
                            }
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = swipeOffset + dragAmount
                            // Allow dragging only if the direction is permitted
                            if ((newOffset > 0 && canGoNext) || (newOffset < 0 && canGoPrevious)) {
                                swipeOffset = newOffset
                                targetSwipeOffset = swipeOffset
                            } else {
                                // Add resistance at boundaries
                                swipeOffset += dragAmount * 0.2f
                                targetSwipeOffset = swipeOffset
                            }
                        }
                    )
                }
                .combinedClickable(
                    onClick = onFlip,
                    onLongClick = {
                        val text = if (displayedIsFlipped) displayedCard.back else displayedCard.front
                        scope.launch {
                            clipboardManager.setClipEntry(ClipEntry.withPlainText(text))
                            snackbarHostState.showSnackbar("Text copied to clipboard")
                        }
                    }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (displayedIsFlipped)
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
                AnimatedContent(targetState = displayedIsFlipped) { flipped ->
                val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val text = if (flipped) displayedCard.back else displayedCard.front
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
        text = if (displayedIsFlipped) "Swipe or tap to flip" else "Tap to reveal answer",
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