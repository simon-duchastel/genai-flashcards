package ai.solenne.flashcards.app.presentation.create

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import ai.solenne.flashcards.shared.domain.model.Flashcard
import kotlinx.serialization.Serializable
import ai.solenne.flashcards.app.parcel.Parcelize

@Serializable
@Parcelize
data object CreateScreen: Screen

// UI State
data class CreateUiState(
    val contentState: ContentState,
    val deleteDialogState: DeleteDialogState,
    val onBackClicked: () -> Unit,
) : CircuitUiState

sealed interface ContentState {
    data class Idle(
        val topic: String,
        val query: String,
        val count: Int,
        val onTopicChanged: (String) -> Unit,
        val onQueryChanged: (String) -> Unit,
        val onCountChanged: (Int) -> Unit,
        val onGenerateClicked: () -> Unit,
    ) : ContentState

    data class Generating(
        val topic: String,
        val query: String,
        val count: Int,
    ) : ContentState

    data class Error(
        val message: String,
        val topic: String,
        val query: String,
        val count: Int,
        val onTopicChanged: (String) -> Unit,
        val onQueryChanged: (String) -> Unit,
        val onCountChanged: (Int) -> Unit,
        val onRetry: () -> Unit,
    ) : ContentState

    data class Generated(
        val topic: String,
        val generatedCards: List<Flashcard>,
        val regenerationState: RegenerationState,
        val onSaveClicked: () -> Unit,
        val onEditCard: (String, String, String) -> Unit,
        val onDeleteCardClick: (Flashcard) -> Unit,
    ) : ContentState
}

sealed interface RegenerationState {
    data class Idle(
        val regenerationPrompt: String,
        val onRegenerationPromptChanged: (String) -> Unit,
        val onRerollClicked: () -> Unit,
    ) : RegenerationState

    data class Regenerating(
        val regenerationPrompt: String,
    ) : RegenerationState
}

sealed interface DeleteDialogState {
    data object Hidden : DeleteDialogState

    data class Visible(
        val card: Flashcard,
        val onCancel: () -> Unit,
        val onConfirm: () -> Unit,
    ) : DeleteDialogState
}
