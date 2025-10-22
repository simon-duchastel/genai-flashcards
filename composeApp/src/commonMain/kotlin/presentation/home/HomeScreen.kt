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
    val deleteDialog: DeleteSetDialog? = null,
    val onCreateNewSet: () -> Unit,
    val onOpenSet: (String) -> Unit,
    val onDeleteSetClick: (FlashcardSet) -> Unit,
    val onRefresh: () -> Unit,
    val onSettingsClick: () -> Unit
) : CircuitUiState

data class DeleteSetDialog(
    val set: FlashcardSet,
    val onCancel: () -> Unit,
    val onConfirm: () -> Unit
)
