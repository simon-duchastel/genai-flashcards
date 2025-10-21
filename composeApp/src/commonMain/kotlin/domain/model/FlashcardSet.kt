package domain.model

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class FlashcardSet(
    val id: String = uuid4().toString(),
    val topic: String,
    val flashcards: List<Flashcard>,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    val cardCount: Int get() = flashcards.size
}
