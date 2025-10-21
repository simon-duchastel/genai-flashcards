package presentation.create

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import data.ai.FlashcardGenerator
import domain.model.Flashcard
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
        val scope = rememberCoroutineScope()

        return CreateUiState(
            topic = topic,
            query = query,
            count = count,
            isGenerating = isGenerating,
            generatedCards = generatedCards,
            error = error,
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
                    error = "Please enter a topic"
                } else {
                    isGenerating = true
                    error = null
                    scope.launch {
                        try {
                            val flashcardSet = generator.generate(topic, count, query.ifBlank { "Generate comprehensive flashcards" })

                            if (flashcardSet == null) {
                                error = """
                                    Failed to generate flashcards. Please try again later.
                                    
                                    Common issues to check for:
                                    - Internet issues
                                    - API key (did you forget to set it?)
                                """.trimIndent()
                                isGenerating = false
                            } else {
                                generatedCards = flashcardSet.flashcards
                                isGenerating = false
                            }
                        } catch (e: Exception) {
                            error = "Error: ${e.message ?: "Failed to generate flashcards"}"
                            isGenerating = false
                        }
                    }
                }
            },
            onSaveClicked = {
                if (generatedCards.isEmpty()) {
                    error = "No flashcards to save"
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
                            error = "Failed to save flashcards: ${e.message}"
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
            onDeleteCard = { cardId ->
                generatedCards = generatedCards.filter { it.id != cardId }
            }
        )
    }
}
