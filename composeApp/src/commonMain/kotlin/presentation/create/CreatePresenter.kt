package presentation.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import domain.model.Flashcard
import domain.repository.FlashcardGenerator
import domain.repository.FlashcardRepository
import kotlinx.coroutines.launch

class CreatePresenter(
    private val screen: CreateScreen,
    private val navigator: Navigator,
    private val repository: FlashcardRepository,
    private val generator: FlashcardGenerator
) : Presenter<CreateUiState> {

    @Composable
    override fun present(): CreateUiState {
        var topic by remember { mutableStateOf(screen.topicHint ?: "") }
        var query by remember { mutableStateOf("") }
        var count by remember { mutableStateOf(10) }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedCards by remember { mutableStateOf(emptyList<Flashcard>()) }
        var error by remember { mutableStateOf<String?>(null) }
        var deleteDialog by remember { mutableStateOf<DeleteCardDialog?>(null) }
        val scope = rememberCoroutineScope()

        return CreateUiState(
            topic = topic,
            query = query,
            count = count,
            isGenerating = isGenerating,
            generatedCards = generatedCards,
            error = error,
            deleteDialog = deleteDialog,
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
                                generatedCards = flashcardSet.flashcards
                                isGenerating = false
                            }
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
                if (generatedCards.isEmpty()) {
                    error = """
                        No flashcards to save

                        Need help? Email help@solenne.ai
                    """.trimIndent()
                } else {
                    scope.launch {
                        try {
                            val flashcardSet = domain.model.FlashcardSet(
                                topic = topic,
                                flashcards = generatedCards
                            )
                            repository.saveFlashcardSet(flashcardSet)
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
                generatedCards = generatedCards.map { card ->
                    if (card.id == cardId) {
                        card.copy(front = front, back = back)
                    } else {
                        card
                    }
                }
            },
            onDeleteCardClick = { card ->
                deleteDialog = DeleteCardDialog(
                    card = card,
                    onCancel = { deleteDialog = null },
                    onConfirm = {
                        deleteDialog = null
                        generatedCards = generatedCards.filter { it.id != card.id }
                    }
                )
            }
        )
    }
}
