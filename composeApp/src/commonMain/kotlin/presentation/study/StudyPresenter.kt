package presentation.study

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import domain.model.Flashcard
import domain.repository.FlashcardRepository
import kotlinx.coroutines.launch

class StudyPresenter(
    private val screen: StudyScreen,
    private val navigator: Navigator,
    private val repository: FlashcardRepository
) : Presenter<StudyUiState> {

    @Composable
    override fun present(): StudyUiState {
        var flashcards by remember { mutableStateOf(emptyList<Flashcard>()) }
        var currentIndex by remember { mutableStateOf(0) }
        var isFlipped by remember { mutableStateOf(false) }
        var topic by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        // Load and shuffle flashcards
        LaunchedEffect(screen.setId) {
            scope.launch {
                val cards = repository.getRandomizedFlashcards(screen.setId)
                if (cards != null) {
                    flashcards = cards
                    val set = repository.getFlashcardSet(screen.setId)
                    topic = set?.topic ?: ""
                } else {
                    // Handle error - set not found
                    navigator.pop()
                }
            }
        }

        return StudyUiState(
            flashcards = flashcards,
            currentIndex = currentIndex,
            isFlipped = isFlipped,
            topic = topic,
            eventSink = { event ->
                when (event) {
                    StudyEvent.FlipCard -> {
                        isFlipped = !isFlipped
                    }
                    StudyEvent.NextCard -> {
                        if (currentIndex < flashcards.size - 1) {
                            currentIndex++
                            isFlipped = false
                        }
                    }
                    StudyEvent.PreviousCard -> {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                        }
                    }
                    StudyEvent.ExitStudy -> {
                        navigator.pop()
                    }
                    StudyEvent.RestartStudy -> {
                        currentIndex = 0
                        isFlipped = false
                        scope.launch {
                            val cards = repository.getRandomizedFlashcards(screen.setId)
                            if (cards != null) {
                                flashcards = cards
                            }
                        }
                    }
                }
            }
        )
    }
}
