package domain.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
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
