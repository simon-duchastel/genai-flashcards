package presentation.create

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import domain.model.Flashcard
import kotlinx.serialization.Serializable
import parcel.Parcelize

@Serializable
@Parcelize
data object CreateScreen: Screen

// UI State
data class CreateUiState(
    val topic: String,
    val query: String,
    val count: Int,
    val isGenerating: Boolean,
    val generatedCards: List<Flashcard>,
    val error: String?,
    val deleteDialog: DeleteCardDialog? = null,
    val regenerationPrompt: String,
    val isRegenerating: Boolean,
    val onTopicChanged: (String) -> Unit,
    val onQueryChanged: (String) -> Unit,
    val onCountChanged: (Int) -> Unit,
    val onGenerateClicked: () -> Unit,
    val onSaveClicked: () -> Unit,
    val onBackClicked: () -> Unit,
    val onEditCard: (String, String, String) -> Unit,
    val onDeleteCardClick: (Flashcard) -> Unit,
    val onRegenerationPromptChanged: (String) -> Unit,
    val onRerollClicked: () -> Unit
) : CircuitUiState

data class DeleteCardDialog(
    val card: Flashcard,
    val onCancel: () -> Unit,
    val onConfirm: () -> Unit
)
