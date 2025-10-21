package presentation.create

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
    val onTopicChanged: (String) -> Unit,
    val onCountChanged: (Int) -> Unit,
    val onGenerateClicked: () -> Unit,
    val onSaveClicked: () -> Unit,
    val onBackClicked: () -> Unit,
    val onEditCard: (String, String, String) -> Unit,
    val onDeleteCard: (String) -> Unit
) : CircuitUiState
