package domain.model

import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Serializable
data class Flashcard(
    val id: String = Uuid.random().toString(),
    val setId: String = "",
    val front: String = "",
    val back: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)
