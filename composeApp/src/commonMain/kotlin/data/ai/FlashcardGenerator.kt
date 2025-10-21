package data.ai

import domain.model.Flashcard
import domain.model.FlashcardSet
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * AI-powered flashcard generator using Koog framework.
 *
 * For now, we'll use a direct OpenAI API call approach.
 * TODO: Integrate full Koog agent framework once we verify JS compatibility.
 */
class FlashcardGenerator(
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    @Serializable
    private data class GeneratedCard(
        val front: String,
        val back: String
    )

    /**
     * Generate flashcards on a given topic.
     *
     * @param topic The subject/topic for the flashcards
     * @param count Number of flashcards to generate
     * @return FlashcardSet with generated cards
     */
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    suspend fun generate(topic: String, count: Int): FlashcardSet {
        // This is a placeholder implementation
        // In a real app, this would call the OpenAI API via Koog
        // For now, we'll return mock data for testing

        val setId = Uuid.random().toString()
        val mockCards = (1..count).map { i ->
            Flashcard(
                front = "Question $i about $topic?",
                back = "Answer $i explaining key concepts about $topic.",
                setId = setId
            )
        }

        return FlashcardSet(
            id = setId,
            topic = topic,
            flashcards = mockCards
        )
    }

    /**
     * TODO: Implement with Koog agent framework
     *
     * Example Koog implementation:
     * ```kotlin
     * private val agent = koogAgent {
     *     model = OpenAI(apiKey = apiKey, model = "gpt-4-turbo")
     *
     *     systemPrompt = """
     *         You are an expert flashcard creator.
     *         Generate $count high-quality flashcards about: $topic
     *
     *         Return ONLY a JSON array in this exact format:
     *         [{"front": "question", "back": "answer"}, ...]
     *
     *         Guidelines:
     *         - Questions should be clear and specific
     *         - Answers should be comprehensive but concise
     *         - Focus on key concepts and important details
     *         - Use active learning principles
     *     """.trimIndent()
     * }
     *
     * val response = agent.chat("Generate the flashcards")
     * return parseResponse(response, topic)
     * ```
     */
}
