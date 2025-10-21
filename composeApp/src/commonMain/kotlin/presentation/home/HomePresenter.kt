package presentation.home

import androidx.compose.runtime.*
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import domain.repository.FlashcardRepository
import kotlinx.coroutines.launch
import presentation.create.CreateScreen
import presentation.study.StudyScreen

class HomePresenter(
    private val navigator: Navigator,
    private val repository: FlashcardRepository
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        var flashcardSets by remember { mutableStateOf(emptyList<domain.model.FlashcardSet>()) }
        var isLoading by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()

        // Load flashcard sets on initial composition
        LaunchedEffect(Unit) {
            loadFlashcardSets { sets ->
                flashcardSets = sets
                isLoading = false
            }
        }

        return HomeUiState(
            flashcardSets = flashcardSets,
            isLoading = isLoading,
            eventSink = { event ->
                when (event) {
                    HomeEvent.CreateNewSet -> {
                        navigator.goTo(CreateScreen())
                    }
                    is HomeEvent.OpenSet -> {
                        navigator.goTo(StudyScreen(setId = event.setId))
                    }
                    is HomeEvent.DeleteSet -> {
                        scope.launch {
                            repository.deleteFlashcardSet(event.setId)
                            loadFlashcardSets { sets ->
                                flashcardSets = sets
                            }
                        }
                    }
                    HomeEvent.Refresh -> {
                        isLoading = true
                        scope.launch {
                            loadFlashcardSets { sets ->
                                flashcardSets = sets
                                isLoading = false
                            }
                        }
                    }
                }
            }
        )
    }

    private suspend fun loadFlashcardSets(onLoaded: (List<domain.model.FlashcardSet>) -> Unit) {
        val sets = repository.getAllFlashcardSets()
        onLoaded(sets)
    }
}
