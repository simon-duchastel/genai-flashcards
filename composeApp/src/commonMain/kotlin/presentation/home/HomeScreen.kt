package presentation.home

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.FlashcardSet
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen : Screen

// UI State
data class HomeUiState(
    val flashcardSets: List<FlashcardSet>,
    val isLoading: Boolean = false,
    val onCreateNewSet: () -> Unit,
    val onOpenSet: (String) -> Unit,
    val onDeleteSet: (String) -> Unit,
    val onRefresh: () -> Unit
) : CircuitUiState
