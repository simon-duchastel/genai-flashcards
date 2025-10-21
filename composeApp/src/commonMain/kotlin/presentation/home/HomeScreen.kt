package presentation.home

import androidx.compose.runtime.*
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
class HomeScreen : Screen

// UI State
data class HomeUiState(
    val flashcardSets: List<FlashcardSet>,
    val isLoading: Boolean = false,
    val eventSink: (HomeEvent) -> Unit
) : CircuitUiState

// Events
sealed interface HomeEvent : CircuitUiEvent {
    data object CreateNewSet : HomeEvent
    data class OpenSet(val setId: String) : HomeEvent
    data class DeleteSet(val setId: String) : HomeEvent
    data object Refresh : HomeEvent
}
