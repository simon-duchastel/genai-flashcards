package ai.solenne.flashcards.app.presentation.study

import androidx.compose.runtime.*
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import ai.solenne.flashcards.shared.domain.model.Flashcard
import kotlinx.serialization.Serializable
import ai.solenne.flashcards.app.parcel.Parcelize

@Serializable
@Parcelize
data class StudyScreen(
    val setId: String
) : Screen

// UI State
data class StudyUiState(
    val contentState: ContentState,
    val onExitStudy: () -> Unit,
) : CircuitUiState

sealed interface ContentState {
    data object Loading : ContentState

    data class Error(
        val message: String,
        val onRetry: () -> Unit,
    ) : ContentState

    data class Loaded(
        val topic: String,
        val studyState: StudyState,
    ) : ContentState
}

sealed interface StudyState {
    data class Studying(
        val flashcards: List<Flashcard>,
        val currentIndex: Int,
        val isFlipped: Boolean,
        val onFlipCard: () -> Unit,
        val onNextCard: () -> Unit,
        val onPreviousCard: () -> Unit,
        val onRestartStudy: () -> Unit,
    ) : StudyState {
        val currentCard: Flashcard? = flashcards.getOrNull(currentIndex)
        val progress: String = "${currentIndex + 1}/${flashcards.size}"
    }

    data class Complete(
        val flashcards: List<Flashcard>,
        val topic: String,
        val onRestartStudy: () -> Unit,
    ) : StudyState
}
