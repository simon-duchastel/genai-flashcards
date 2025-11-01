package domain.generator

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.requestLLMStructured
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.markdown.markdown
import ai.koog.prompt.structure.StructureFixingParser
import domain.model.Flashcard
import domain.model.FlashcardSet
import domain.model.FlashcardSetRaw
import domain.repository.FlashcardGenerator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Koog AI-powered implementation of FlashcardGenerator.
 * Uses Gemini API for flashcard generation.
 *
 * This implementation is shared between client and server.
 *
 * @param getGeminiApiKey function for retrieving the API key for connecting to the Google Gemini API.
 */
class KoogFlashcardGenerator(
    private val getGeminiApiKey: suspend () -> String?
) : FlashcardGenerator {

    /**
     * Create a Koog agent configured for flashcard generation.
     * Returns FlashcardSetRaw without id, userId, or createdAt fields.
     */
    private fun createAgent(apiKey: String): AIAgent<String, FlashcardSetRaw?> {
        return AIAgent(
            systemPrompt = """
                You are a flashcard generation assistant. Your task is to create educational flashcards
                that help students learn and test their understanding of a given topic.

                For each flashcard:
                - front: Create a clear, specific question that tests understanding of a key concept
                - back: Provide a concise, accurate answer that explains the concept

                Focus on:
                - Important concepts and definitions
                - Key facts and principles
                - Practical applications
                - Common misconceptions to clarify

                Return your response in structured JSON format.

                Generate exactly the number of flashcards requested, ensuring variety and comprehensive coverage.
            """.trimIndent(),
            promptExecutor = MultiLLMPromptExecutor(
                LLMProvider.Google to GoogleLLMClient(apiKey)
            ),
            strategy = functionalStrategy { query ->
                requestLLMStructured<FlashcardSetRaw>(
                    message = query,
                    fixingParser = StructureFixingParser(
                        fixingModel = GoogleModels.Gemini2_5Flash,
                        retries = 5,
                    )
                ).getOrNull()?.structure
            },
            llmModel = GoogleModels.Gemini2_5Pro
        )
    }

    /**
     * Convert a FlashcardSetRaw to a full FlashcardSet by adding metadata.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun FlashcardSetRaw.toFlashcardSet(): FlashcardSet {
        val setId = Uuid.random().toString()
        val createdAt = Clock.System.now().toEpochMilliseconds()

        return FlashcardSet(
            id = setId,
            userId = null, // Will be set later by the caller (server or client)
            topic = this.topic,
            flashcards = this.flashcards.map { rawCard ->
                Flashcard(
                    id = Uuid.random().toString(),
                    setId = setId,
                    front = rawCard.front,
                    back = rawCard.back,
                    createdAt = createdAt
                )
            },
            createdAt = createdAt
        )
    }

    /**
     * Generate flashcards using the provided or default API key.
     *
     * @param topic The subject/topic for the flashcards
     * @param count Number of flashcards to generate
     * @param userQuery Additional information to guide generation
     * @return FlashcardSet with generated cards, or null if generation fails
     */
    override suspend fun generate(topic: String, count: Int, userQuery: String): FlashcardSet? {
        return try {
            val apiKey = getGeminiApiKey() ?: return null
            val agent = createAgent(apiKey)

            val prompt = markdown {
                +"Please generate exactly $count flashcards on the topic of '$topic'"

                h2("Additional Information")
                +"Here is more information to help guide you:"
                +userQuery
            }

            val rawSet = agent.run(prompt) ?: return null
            rawSet.toFlashcardSet()
        } catch (e: Exception) {
            println("Failed to generate flashcards: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Regenerate flashcards based on an existing set.
     *
     * @param existingSet The current flashcard set to improve upon
     * @param regenerationPrompt User's custom instruction for regeneration
     * @param count Number of flashcards to generate
     * @return FlashcardSet with regenerated cards, or null if generation fails
     */
    override suspend fun regenerate(existingSet: FlashcardSet, regenerationPrompt: String): FlashcardSet? {
        return try {
            val apiKey = getGeminiApiKey() ?: return null
            val agent = createAgent(apiKey)

            val prompt = markdown {
                +"Please regenerate exactly ${existingSet.cardCount} flashcards on the topic of '${existingSet.topic}'."
                +"Create NEW questions and answers that cover similar concepts but are worded differently."

                h2("Existing Flashcards")
                existingSet.flashcards.forEachIndexed { index, card ->
                    h3("Card ${index + 1}")
                    +"**Front:** ${card.front}"
                    +"**Back:** ${card.back}"
                    +""
                }

                if (regenerationPrompt.isNotBlank()) {
                    h2("Additional User Instructions")
                    +regenerationPrompt
                }

                +""
                +"Remember: Generate ${existingSet.cardCount} FRESH flashcards with new wording that test understanding of '${existingSet.topic}' in different ways."
            }

            val rawSet = agent.run(prompt) ?: return null
            rawSet.toFlashcardSet()
        } catch (e: Exception) {
            println("Failed to regenerate flashcards: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
