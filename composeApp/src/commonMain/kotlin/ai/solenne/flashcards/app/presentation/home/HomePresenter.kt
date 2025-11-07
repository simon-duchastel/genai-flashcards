package ai.solenne.flashcards.app.presentation.home

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import ai.solenne.flashcards.app.domain.model.FlashcardSetWithMeta
import ai.solenne.flashcards.app.domain.repository.AuthRepository
import ai.solenne.flashcards.app.domain.repository.ClientFlashcardRepository
import ai.solenne.flashcards.app.domain.repository.LocalFlashcardRepository
import kotlinx.coroutines.launch
import ai.solenne.flashcards.app.presentation.auth.AuthScreen
import ai.solenne.flashcards.app.presentation.create.CreateScreen
import ai.solenne.flashcards.app.presentation.study.StudyScreen

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
        var error by remember { mutableStateOf<String?>(null) }
        var deleteDialogSet by remember { mutableStateOf<FlashcardSetWithMeta?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            loadFlashcardSets(
                onLoaded = { sets ->
                    flashcardSets = sets
                    isLoading = false
                    error = null
                },
                onError = { errorMsg ->
                    error = errorMsg
                    isLoading = false
                }
            )
        }

        val currentError = error
        val contentState: ContentState = when {
            isLoading -> ContentState.Loading
            currentError != null -> ContentState.Error(
                message = currentError,
                onRetry = {
                    scope.launch {
                        isLoading = true
                        error = null
                        loadFlashcardSets(
                            onLoaded = { sets ->
                                flashcardSets = sets
                                isLoading = false
                                error = null
                            },
                            onError = { errorMsg ->
                                error = errorMsg
                                isLoading = false
                            }
                        )
                    }
                }
            )
            else -> ContentState.Loaded(
                flashcardSets = flashcardSets,
                onOpenSet = { setId ->
                    navigator.goTo(StudyScreen(setId = setId))
                },
                onDeleteSetClick = { set ->
                    deleteDialogSet = set
                },
                onRefresh = {
                    isLoading = true
                    scope.launch {
                        loadFlashcardSets(
                            onLoaded = { sets ->
                                flashcardSets = sets
                                isLoading = false
                                error = null
                            },
                            onError = { errorMsg ->
                                error = errorMsg
                                isLoading = false
                            }
                        )
                    }
                }
            )
        }

        val deleteSet = deleteDialogSet
        val deleteDialogState: DeleteDialogState = if (deleteSet != null) {
            DeleteDialogState.Visible(
                set = deleteSet,
                onCancel = { deleteDialogSet = null },
                onConfirm = {
                    deleteDialogSet = null
                    scope.launch {
                        deleteFlashcardSet(deleteSet.flashcardSet.id)
                        loadFlashcardSets(
                            onLoaded = { sets ->
                                flashcardSets = sets
                                error = null
                            },
                            onError = { errorMsg ->
                                error = errorMsg
                            }
                        )
                    }
                }
            )
        } else {
            DeleteDialogState.Hidden
        }

        return HomeUiState(
            contentState = contentState,
            deleteDialogState = deleteDialogState,
            onCreateNewSet = {
                navigator.goTo(CreateScreen)
            },
            onSettingsClick = {
                navigator.goTo(AuthScreen)
            }
        )
    }

    private suspend fun loadFlashcardSets(
        onLoaded: (List<FlashcardSetWithMeta>) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
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
        } catch (e: Exception) {
            onError(e.message ?: "Failed to load flashcard sets")
        }
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
