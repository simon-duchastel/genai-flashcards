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
        var count by remember { mutableStateOf(10) }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedCards by remember { mutableStateOf(emptyList<Flashcard>()) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        return CreateUiState(
            topic = topic,
            count = count,
            isGenerating = isGenerating,
            generatedCards = generatedCards,
            error = error,
            eventSink = { event ->
                when (event) {
                    is CreateEvent.TopicChanged -> {
                        topic = event.topic
                        error = null
                    }
                    is CreateEvent.CountChanged -> {
                        count = event.count.coerceIn(5, 50)
                    }
                    CreateEvent.GenerateClicked -> {
                        if (topic.isBlank()) {
                            error = "Please enter a topic"
                            return@CreateUiState
                        }

                        isGenerating = true
                        error = null
                        scope.launch {
                            try {
                                val flashcardSet = generator.generate(topic, count, "Make it good")
                                generatedCards = flashcardSet.flashcards
                                isGenerating = false
                            } catch (e: Exception) {
                                error = "Failed to generate flashcards: ${e.message}"
                                isGenerating = false
                            }
                        }
                    }
                    CreateEvent.SaveClicked -> {
                        if (generatedCards.isEmpty()) {
                            error = "No flashcards to save"
                            return@CreateUiState
                        }

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
                    CreateEvent.BackClicked -> {
                        navigator.pop()
                    }
                    is CreateEvent.EditCard -> {
                        generatedCards = generatedCards.map { card ->
                            if (card.id == event.cardId) {
                                card.copy(front = event.front, back = event.back)
                            } else {
                                card
                            }
                        }
                    }
                    is CreateEvent.DeleteCard -> {
                        generatedCards = generatedCards.filter { it.id != event.cardId }
                    }
                }
            }
        )
    }
}
