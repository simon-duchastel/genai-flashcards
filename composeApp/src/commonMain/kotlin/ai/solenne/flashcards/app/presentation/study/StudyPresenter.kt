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
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        // Load and shuffle flashcards
        LaunchedEffect(screen.setId) {
            scope.launch {
                isLoading = true
                error = null
                try {
                    val set = getFlashcardSet(screen.setId)
                    if (set != null) {
                        flashcards = set.flashcards.shuffled()
                        topic = set.topic
                        isLoading = false
                    } else {
                        error = "Flashcard set not found"
                        isLoading = false
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Failed to load flashcards"
                    isLoading = false
                }
            }
        }

        val currentError = error
        val contentState: ContentState = when {
            isLoading -> ContentState.Loading
            currentError != null -> ContentState.Error(
                message = currentError,
                onRetry = {
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            val set = getFlashcardSet(screen.setId)
                            if (set != null) {
                                flashcards = set.flashcards.shuffled()
                                topic = set.topic
                                isLoading = false
                            } else {
                                error = "Flashcard set not found"
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to load flashcards"
                            isLoading = false
                        }
                    }
                }
            )
            else -> {
                val studyState: StudyState = if (currentIndex >= flashcards.size) {
                    StudyState.Complete(
                        flashcards = flashcards,
                        topic = topic,
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
                } else {
                    StudyState.Studying(
                        flashcards = flashcards,
                        currentIndex = currentIndex,
                        isFlipped = isFlipped,
                        onFlipCard = {
                            isFlipped = !isFlipped
                        },
                        onNextCard = {
                            if (currentIndex < flashcards.size - 1) {
                                currentIndex++
                                isFlipped = false
                            } else {
                                // Move to complete state
                                currentIndex = flashcards.size
                                isFlipped = false
                            }
                        },
                        onPreviousCard = {
                            if (currentIndex > 0) {
                                currentIndex--
                                isFlipped = false
                            }
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
                ContentState.Loaded(
                    topic = topic,
                    studyState = studyState
                )
            }
        }

        return StudyUiState(
            contentState = contentState,
            onExitStudy = {
                navigator.pop()
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
