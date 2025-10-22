package domain.model

import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Serializable
data class FlashcardSet(
    val id: String = Uuid.random().toString(),
    val userId: String,
    val topic: String,
    val flashcards: List<Flashcard>,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    val cardCount: Int get() = flashcards.size
}
