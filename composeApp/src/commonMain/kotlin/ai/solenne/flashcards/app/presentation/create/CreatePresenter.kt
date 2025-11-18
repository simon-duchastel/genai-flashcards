package ai.solenne.flashcards.app.presentation.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import ai.solenne.flashcards.app.data.api.ServerFlashcardGenerator.RateLimitException
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import ai.solenne.flashcards.app.domain.repository.AuthRepository
import ai.solenne.flashcards.app.domain.repository.ClientFlashcardRepository
import ai.solenne.flashcards.shared.domain.repository.FlashcardGenerator
import ai.solenne.flashcards.app.domain.repository.LocalFlashcardRepository
import ai.solenne.flashcards.shared.domain.model.Flashcard
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds

class CreatePresenter(
    private val screen: CreateScreen,
    private val navigator: Navigator,
    private val authRepository: AuthRepository,
    private val clientRepository: ClientFlashcardRepository,
    private val localRepository: LocalFlashcardRepository,
    private val serverGenerator: FlashcardGenerator,
    private val koogGenerator: FlashcardGenerator
) : Presenter<CreateUiState> {

    @Composable
    override fun present(): CreateUiState {
        val isEditMode = screen.editSetId != null
        var topic by remember { mutableStateOf("") }
        var query by remember { mutableStateOf("") }
        var count by remember { mutableStateOf(10) }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedCardsSet: FlashcardSet? by remember { mutableStateOf(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var deleteDialogCard by remember { mutableStateOf<Flashcard?>(null) }
        var regenerationPrompt by remember { mutableStateOf("") }
        var isRegenerating by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Load existing flashcard set if in edit mode
        LaunchedEffect(screen.editSetId) {
            if (isEditMode && screen.editSetId != null) {
                try {
                    val existingSet = if (authRepository.isSignedIn()) {
                        clientRepository.getFlashcardSet(screen.editSetId)
                    } else {
                        localRepository.getFlashcardSet(screen.editSetId)
                    }

                    if (existingSet != null) {
                        generatedCardsSet = existingSet
                        topic = existingSet.topic
                    } else {
                        error = """
                            Failed to load flashcard set.

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                    }
                } catch (e: Exception) {
                    error = """
                        Error loading flashcard set: ${e.message}

                        Need help? Email help@solenne.ai
                    """.trimIndent()
                }
            }
        }

        val onTopicChanged: (String) -> Unit = { newTopic ->
            topic = newTopic.take(30)
            error = null
        }

        val onQueryChanged: (String) -> Unit = { newQuery ->
            query = newQuery
            error = null
        }

        val onCountChanged: (Int) -> Unit = { newCount ->
            count = newCount.coerceIn(5, 50)
        }

        val onGenerateClicked: () -> Unit = {
            if (topic.isBlank()) {
                error = """
                Please enter a topic.

                Need help? Email help@solenne.ai
                """.trimIndent()
            } else {
                isGenerating = true
                error = null
                scope.launch {
                    try {
                        // Use server generator if authenticated, otherwise use local (Koog)
                        val generator = if (authRepository.isSignedIn()) {
                            serverGenerator
                        } else {
                            koogGenerator
                        }

                        val flashcardSet = generator.generate(
                            topic = topic,
                            count = count,
                            userQuery = query.ifBlank { "Generate comprehensive flashcards" },
                        )

                        if (flashcardSet == null) {
                            error = """
                                Failed to generate flashcards. Please try again later.

                                Common issues to check for:
                                - Internet issues
                                - API key (did you forget to set it?)

                                Need help? Email help@solenne.ai
                            """.trimIndent()
                            isGenerating = false
                        } else {
                            generatedCardsSet = flashcardSet
                            isGenerating = false
                        }
                    } catch (e: RateLimitException) {
                        val tryAgainDate = fromEpochMilliseconds(e.tryAgainAt)
                        error = """
                            ${e.message}

                            You've used ${e.numberOfGenerations} generations today.
                            You can try again at $tryAgainDate

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                        isGenerating = false
                    } catch (e: Exception) {
                        error = """
                            Error: ${e.message ?: "Failed to generate flashcards"}

                            Need help? Email help@solenne.ai
                        """.trimIndent()
                        isGenerating = false
                    }
                }
            }
        }

        val currentSet = generatedCardsSet
        val currentError = error
        val contentState: ContentState = when {
            isGenerating -> ContentState.Generating(
                topic = topic,
                query = query,
                count = count
            )
            currentError != null -> ContentState.Error(
                message = currentError,
                topic = topic,
                query = query,
                count = count,
                onTopicChanged = onTopicChanged,
                onQueryChanged = onQueryChanged,
                onCountChanged = onCountChanged,
                onRetry = onGenerateClicked
            )
            currentSet != null -> {
                val regenerationState: RegenerationState = if (isRegenerating) {
                    RegenerationState.Regenerating(
                        regenerationPrompt = regenerationPrompt
                    )
                } else {
                    RegenerationState.Idle(
                        regenerationPrompt = regenerationPrompt,
                        onRegenerationPromptChanged = { newPrompt ->
                            regenerationPrompt = newPrompt
                            error = null
                        },
                        onRerollClicked = {
                            isRegenerating = true
                            error = null
                            scope.launch {
                                try {
                                    // Use server generator if authenticated, otherwise use local
                                    val generator = if (authRepository.isSignedIn()) {
                                        serverGenerator
                                    } else {
                                        koogGenerator
                                    }

                                    val newSet = generator.regenerate(
                                        existingSet = currentSet,
                                        regenerationPrompt = regenerationPrompt.ifBlank { "" }
                                    )

                                    if (newSet == null) {
                                        error = """
                                            Failed to regenerate flashcards. Please try again.

                                            Need help? Email help@solenne.ai
                                        """.trimIndent()
                                    } else {
                                        generatedCardsSet = newSet
                                        regenerationPrompt = "" // Clear after successful regeneration
                                    }
                                } catch (e: RateLimitException) {
                                    val tryAgainDate = fromEpochMilliseconds(e.tryAgainAt)
                                    error = """
                                        ${e.message}

                                        You've used ${e.numberOfGenerations} generations today.
                                        You can try again at $tryAgainDate

                                        Need help? Email help@solenne.ai
                                    """.trimIndent()
                                } catch (e: Exception) {
                                    error = """
                                        Error regenerating: ${e.message ?: "Unknown error"}

                                        Need help? Email help@solenne.ai
                                    """.trimIndent()
                                } finally {
                                    isRegenerating = false
                                }
                            }
                        }
                    )
                }

                ContentState.Generated(
                    topic = currentSet.topic,
                    generatedCards = currentSet.flashcards,
                    regenerationState = regenerationState,
                    isEditMode = isEditMode,
                    onSaveClicked = remember(generatedCardsSet, isEditMode) {
                        {
                            scope.launch {
                                try {
                                    // Save to server if authenticated, otherwise save locally
                                    if (authRepository.isSignedIn()) {
                                        try {
                                            if (isEditMode) {
                                                // Use PUT for updating existing set
                                                clientRepository.updateFlashcardSet(currentSet)
                                            } else {
                                                // Use POST for creating new set
                                                clientRepository.saveFlashcardSet(currentSet)
                                            }
                                            runCatching {
                                                localRepository.deleteFlashcardSet(
                                                    currentSet.id
                                                )
                                            }
                                        } catch (e: Exception) {
                                            println("Failed to save to server, saving locally: $e")
                                            localRepository.saveFlashcardSet(currentSet)
                                        }
                                    } else {
                                        localRepository.saveFlashcardSet(currentSet)
                                    }
                                    navigator.pop()
                                } catch (e: Exception) {
                                    error = """
                                    Failed to save flashcards: ${e.message}

                                    Need help? Email help@solenne.ai
                                """.trimIndent()
                                }
                            }
                        }
                    },
                    onEditCard = { cardId, front, back ->
                        val currentCards = currentSet.flashcards
                        generatedCardsSet = currentSet.copy(
                            flashcards = currentCards.map { card ->
                                if (card.id == cardId) {
                                    card.copy(front = front, back = back)
                                } else {
                                    card
                                }
                            }
                        )
                    },
                    onDeleteCardClick = { cardToDelete ->
                        deleteDialogCard = cardToDelete
                    }
                )
            }
            else -> ContentState.Idle(
                topic = topic,
                query = query,
                count = count,
                onTopicChanged = onTopicChanged,
                onQueryChanged = onQueryChanged,
                onCountChanged = onCountChanged,
                onGenerateClicked = onGenerateClicked
            )
        }

        val deleteCard = deleteDialogCard
        val deleteDialogState: DeleteDialogState = if (deleteCard != null) {
            DeleteDialogState.Visible(
                card = deleteCard,
                onCancel = { deleteDialogCard = null },
                onConfirm = {
                    deleteDialogCard = null
                    val currentCards = generatedCardsSet?.flashcards ?: emptyList()
                    generatedCardsSet = generatedCardsSet?.copy(
                        flashcards = currentCards.filter { card ->
                            card.id != deleteCard.id
                        }
                    )
                }
            )
        } else {
            DeleteDialogState.Hidden
        }

        return CreateUiState(
            contentState = contentState,
            deleteDialogState = deleteDialogState,
            isEditMode = isEditMode,
            onBackClicked = {
                navigator.pop()
            }
        )
    }
}
