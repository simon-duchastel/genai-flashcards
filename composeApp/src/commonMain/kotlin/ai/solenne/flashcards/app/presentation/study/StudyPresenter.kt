package ai.solenne.flashcards.app.presentation.study

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.app.domain.repository.AuthRepository
import ai.solenne.flashcards.app.domain.repository.ClientFlashcardRepository
import ai.solenne.flashcards.app.domain.repository.LocalFlashcardRepository
import kotlinx.coroutines.launch

class StudyPresenter(
    private val screen: StudyScreen,
    private val navigator: Navigator,
    private val authRepository: AuthRepository,
    private val clientRepository: ClientFlashcardRepository,
    private val localRepository: LocalFlashcardRepository
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
                val set = getFlashcardSet(screen.setId)
                if (set != null) {
                    flashcards = set.flashcards.shuffled()
                    topic = set.topic
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
            onFlipCard = {
                isFlipped = !isFlipped
            },
            onNextCard = {
                if (currentIndex < flashcards.size - 1) {
                    currentIndex++
                    isFlipped = false
                }
            },
            onPreviousCard = {
                if (currentIndex > 0) {
                    currentIndex--
                    isFlipped = false
                }
            },
            onExitStudy = {
                navigator.pop()
            },
            onRestartStudy = {
                currentIndex = 0
                isFlipped = false
                scope.launch {
                    val set = getFlashcardSet(screen.setId)
                    if (set != null) {
                        flashcards = set.flashcards.shuffled()
                    }
                }
            }
        )
    }

    private suspend fun getFlashcardSet(id: String) =
        // Try server first if authenticated
        if (authRepository.isSignedIn()) {
            try {
                clientRepository.getFlashcardSet(id)
            } catch (e: Exception) {
                println("Failed to load from server, trying local: $e")
                localRepository.getFlashcardSet(id)
            }
        } else {
            localRepository.getFlashcardSet(id)
        } ?: localRepository.getFlashcardSet(id) // Final fallback
}
