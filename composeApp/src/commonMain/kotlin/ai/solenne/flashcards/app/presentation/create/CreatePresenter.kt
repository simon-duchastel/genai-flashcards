package ai.solenne.flashcards.app.presentation.create

import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds

class CreatePresenter(
    private val navigator: Navigator,
    private val authRepository: AuthRepository,
    private val clientRepository: ClientFlashcardRepository,
    private val localRepository: LocalFlashcardRepository,
    private val serverGenerator: FlashcardGenerator,
    private val koogGenerator: FlashcardGenerator
) : Presenter<CreateUiState> {

    @Composable
    override fun present(): CreateUiState {
        var topic by remember { mutableStateOf("") }
        var query by remember { mutableStateOf("") }
        var count by remember { mutableStateOf(10) }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedCardsSet: FlashcardSet? by remember { mutableStateOf(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var deleteDialog by remember { mutableStateOf<DeleteCardDialog?>(null) }
        var regenerationPrompt by remember { mutableStateOf("") }
        var isRegenerating by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        return CreateUiState(
            topic = topic,
            query = query,
            count = count,
            isGenerating = isGenerating,
            generatedCards = generatedCardsSet?.flashcards ?: emptyList(),
            error = error,
            deleteDialog = deleteDialog,
            regenerationPrompt = regenerationPrompt,
            isRegenerating = isRegenerating,
            onTopicChanged = { newTopic ->
                topic = newTopic.take(30)
                error = null
            },
            onQueryChanged = { newQuery ->
                query = newQuery
                error = null
            },
            onCountChanged = { newCount ->
                count = newCount.coerceIn(5, 50)
            },
            onGenerateClicked = {
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
            },
            onSaveClicked = {
                val setToSave = generatedCardsSet
                if (setToSave?.flashcards.isNullOrEmpty()) {
                    error = """
                        No flashcards to save

                        Need help? Email help@solenne.ai
                    """.trimIndent()
                } else {
                    scope.launch {
                        try {
                            // Save to server if authenticated, otherwise save locally
                            if (authRepository.isSignedIn()) {
                                try {
                                    clientRepository.saveFlashcardSet(setToSave)
                                    runCatching { localRepository.deleteFlashcardSet(setToSave.id) }
                                } catch (e: Exception) {
                                    println("Failed to save to server, saving locally: $e")
                                    localRepository.saveFlashcardSet(setToSave)
                                }
                            } else {
                                localRepository.saveFlashcardSet(setToSave)
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
            onBackClicked = {
                navigator.pop()
            },
            onEditCard = { cardId, front, back ->
                val currentCards = generatedCardsSet?.flashcards ?: emptyList()
                generatedCardsSet = generatedCardsSet?.copy(
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
                deleteDialog = DeleteCardDialog(
                    card = cardToDelete,
                    onCancel = { deleteDialog = null },
                    onConfirm = {
                        deleteDialog = null
                        val currentCards = generatedCardsSet?.flashcards ?: emptyList()
                        generatedCardsSet = generatedCardsSet?.copy(
                            flashcards = currentCards.filter { card ->
                                card.id != cardToDelete.id
                            }
                        )
                    }
                )
            },
            onRegenerationPromptChanged = { newPrompt ->
                regenerationPrompt = newPrompt
                error = null
            },
            onRerollClicked = {
                val setToRegenerate = generatedCardsSet
                if (setToRegenerate == null) {
                    error = "No flashcards to regenerate"
                } else {
                    isRegenerating = true
                    error = null
                    scope.launch {
                        try {
                            // Use server generator if authenticated, otherwise use local (Koog)
                            val generator = if (authRepository.isSignedIn()) {
                                serverGenerator
                            } else {
                                koogGenerator
                            }

                            val newSet = generator.regenerate(
                                existingSet = setToRegenerate,
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
            }
        )
    }
}
