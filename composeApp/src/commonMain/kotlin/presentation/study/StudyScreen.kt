package presentation.study

import androidx.compose.runtime.*
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.Flashcard
import kotlinx.serialization.Serializable

@Serializable
data class StudyScreen(
    val setId: String
) : Screen

// UI State
data class StudyUiState(
    val flashcards: List<Flashcard>,
    val currentIndex: Int,
    val isFlipped: Boolean,
    val topic: String,
    val onFlipCard: () -> Unit,
    val onNextCard: () -> Unit,
    val onPreviousCard: () -> Unit,
    val onExitStudy: () -> Unit,
    val onRestartStudy: () -> Unit
) : CircuitUiState {
    val currentCard: Flashcard? = flashcards.getOrNull(currentIndex)
    val progress: String = "${currentIndex + 1}/${flashcards.size}"
    val isComplete: Boolean = currentIndex >= flashcards.size
}
