package presentation.home

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import domain.repository.FlashcardRepository
import kotlinx.coroutines.launch
import presentation.auth.AuthScreen
import presentation.create.CreateScreen
import presentation.study.StudyScreen

class HomePresenter(
    private val navigator: Navigator,
    private val repository: FlashcardRepository
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var flashcardSets by remember { mutableStateOf(emptyList<domain.model.FlashcardSetWithMeta>()) }
        var isLoading by remember { mutableStateOf(true) }
        var deleteDialog by remember { mutableStateOf<DeleteSetDialog?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            loadFlashcardSets { sets ->
                flashcardSets = sets
                isLoading = false
            }
        }

        return HomeUiState(
            flashcardSets = flashcardSets,
            isLoading = isLoading,
            deleteDialog = deleteDialog,
            onCreateNewSet = {
                navigator.goTo(CreateScreen())
            },
            onOpenSet = { setId ->
                navigator.goTo(StudyScreen(setId = setId))
            },
            onDeleteSetClick = { set ->
                deleteDialog = DeleteSetDialog(
                    set = set,
                    onCancel = { deleteDialog = null },
                    onConfirm = {
                        deleteDialog = null
                        scope.launch {
                            repository.deleteFlashcardSet(set.flashcardSet.id)
                            loadFlashcardSets { sets ->
                                flashcardSets = sets
                            }
                        }
                    }
                )
            },
            onRefresh = {
                isLoading = true
                scope.launch {
                    loadFlashcardSets { sets ->
                        flashcardSets = sets
                        isLoading = false
                    }
                }
            },
            onSettingsClick = {
                navigator.goTo(AuthScreen)
            }
        )
    }

    private suspend fun loadFlashcardSets(onLoaded: (List<domain.model.FlashcardSetWithMeta>) -> Unit) {
        val sets = repository.getAllFlashcardSets()
        onLoaded(sets)
    }
}
