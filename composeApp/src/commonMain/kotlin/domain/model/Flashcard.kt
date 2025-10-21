package domain.model

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Flashcard(
    val id: String = uuid4().toString(),
    val front: String,
    val back: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val setId: String
)
