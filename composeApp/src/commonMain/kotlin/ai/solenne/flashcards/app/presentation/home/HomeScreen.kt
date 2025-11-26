package ai.solenne.flashcards.app.presentation.home

import ai.solenne.flashcards.app.parcel.Parcelize
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import ai.solenne.flashcards.app.domain.model.FlashcardSetWithMeta
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data object HomeScreen : Screen

// UI State
data class HomeUiState(
    val contentState: ContentState,
    val deleteDialogState: DeleteDialogState,
    val onCreateNewSet: () -> Unit,
    val onSettingsClick: () -> Unit,
) : CircuitUiState

sealed interface ContentState {
    data object Loading : ContentState

    data class Error(
        val message: String,
        val onRetry: () -> Unit,
    ) : ContentState

    data class Loaded(
        val flashcardSets: List<FlashcardSetWithMeta>,
        val onOpenSet: (String) -> Unit,
        val onEditSetClick: (FlashcardSetWithMeta) -> Unit,
        val onDeleteSetClick: (FlashcardSetWithMeta) -> Unit,
        val onRefresh: () -> Unit,
    ) : ContentState
}

sealed interface DeleteDialogState {
    data object Hidden : DeleteDialogState

    data class Visible(
        val set: FlashcardSetWithMeta,
        val onCancel: () -> Unit,
        val onConfirm: () -> Unit,
    ) : DeleteDialogState
}
