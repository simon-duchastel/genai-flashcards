package presentation.home

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import domain.model.FlashcardSetWithMeta
import domain.repository.AuthRepository
import domain.repository.ClientFlashcardRepository
import domain.repository.LocalFlashcardRepository
import kotlinx.coroutines.launch
import presentation.auth.AuthScreen
import presentation.create.CreateScreen
import presentation.study.StudyScreen

class HomePresenter(
    private val navigator: Navigator,
    private val authRepository: AuthRepository,
    private val clientRepository: ClientFlashcardRepository,
    private val localRepository: LocalFlashcardRepository
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var flashcardSets by remember { mutableStateOf(emptyList<FlashcardSetWithMeta>()) }
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
                navigator.goTo(CreateScreen)
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
                            deleteFlashcardSet(set.flashcardSet.id)
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

    private suspend fun loadFlashcardSets(onLoaded: (List<FlashcardSetWithMeta>) -> Unit) {
        // Get sets from server if signed in
        val serverSets = if (authRepository.isSignedIn()) {
            try {
                clientRepository.getAllFlashcardSets()
            } catch (e: Exception) {
                println("Failed to load server flashcards: $e")
                emptyList()
            }
        } else {
            emptyList()
        }

        // Get local sets
        val localSets = try {
            localRepository.getAllFlashcardSets()
        } catch (e: Exception) {
            println("Failed to load local flashcards: $e")
            emptyList()
        }

        val serverIds = serverSets.map { it.id }.toSet()
        val localOnlySets = localSets.filter { it.id !in serverIds }

        val merged = serverSets.map { FlashcardSetWithMeta(it, isLocalOnly = false) } +
                     localOnlySets.map { FlashcardSetWithMeta(it, isLocalOnly = true) }

        onLoaded(merged.sortedByDescending { it.flashcardSet.createdAt })
    }

    private suspend fun deleteFlashcardSet(id: String) {
        // Delete from server if signed in
        if (authRepository.isSignedIn()) {
            try {
                clientRepository.deleteFlashcardSet(id)
            } catch (e: Exception) {
                println("Failed to delete from server: $e")
            }
        }

        // Always delete from local storage
        try {
            localRepository.deleteFlashcardSet(id)
        } catch (e: Exception) {
            println("Failed to delete from local storage: $e")
        }
    }
}
