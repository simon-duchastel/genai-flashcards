package domain.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class Flashcard(
    val id: String = Uuid.random().toString(),
    val setId: String = "",
    val front: String = "",
    val back: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)
