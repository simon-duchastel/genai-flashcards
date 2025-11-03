package ai.solenne.flashcards.shared.domain.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class FlashcardSet(
    val id: String = Uuid.random().toString(),
    val userId: String? = null, // userId of the user who created this set, or null if it was created anonymously
    val topic: String = "",
    val flashcards: List<Flashcard> = emptyList(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    val cardCount: Int get() = flashcards.size
}
