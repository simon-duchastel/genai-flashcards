package presentation.create

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.Flashcard
import kotlinx.serialization.Serializable

@Serializable
data class CreateScreen(
    val topicHint: String? = null
) : Screen

// UI State
data class CreateUiState(
    val topic: String,
    val count: Int,
    val isGenerating: Boolean,
    val generatedCards: List<Flashcard>,
    val error: String?,
    val eventSink: (CreateEvent) -> Unit
) : CircuitUiState

// Events
sealed interface CreateEvent : CircuitUiEvent {
    data class TopicChanged(val topic: String) : CreateEvent
    data class CountChanged(val count: Int) : CreateEvent
    data object GenerateClicked : CreateEvent
    data object SaveClicked : CreateEvent
    data object BackClicked : CreateEvent
    data class EditCard(val cardId: String, val front: String, val back: String) : CreateEvent
    data class DeleteCard(val cardId: String) : CreateEvent
}
