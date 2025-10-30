package presentation.home

import parcel.Parcelize
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.FlashcardSetWithMeta
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data object HomeScreen : Screen

// UI State
data class HomeUiState(
    val flashcardSets: List<FlashcardSetWithMeta>,
    val isLoading: Boolean = false,
    val deleteDialog: DeleteSetDialog? = null,
    val onCreateNewSet: () -> Unit,
    val onOpenSet: (String) -> Unit,
    val onDeleteSetClick: (FlashcardSetWithMeta) -> Unit,
    val onRefresh: () -> Unit,
    val onSettingsClick: () -> Unit
) : CircuitUiState

data class DeleteSetDialog(
    val set: FlashcardSetWithMeta,
    val onCancel: () -> Unit,
    val onConfirm: () -> Unit
)
