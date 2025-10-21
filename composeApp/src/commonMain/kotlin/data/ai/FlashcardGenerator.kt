package data.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.requestLLMStructured
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.markdown.markdown
import ai.koog.prompt.structure.StructureFixingParser
import data.storage.ConfigRepository
import domain.model.FlashcardSet
import kotlin.uuid.ExperimentalUuidApi

/**
 * AI-powered flashcard generator using Koog framework.
 */
class FlashcardGenerator(
    private val configRepository: ConfigRepository,
) {
    /**
     * Koog agent configured for flashcard generation using functional strategy.
     */
    private fun createAgent(geminiApiKey: String): AIAgent<String, FlashcardSet?> {
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
                LLMProvider.Google to GoogleLLMClient(geminiApiKey)
            ),
            strategy = functionalStrategy { query ->
                requestLLMStructured<FlashcardSet>(
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
     * Generate flashcards on a given topic.
     *
     * @param topic The subject/topic for the flashcards
     * @param count Number of flashcards to generate
     * @return FlashcardSet with generated cards, or null if generation fails
     */
    suspend fun generate(topic: String, count: Int, userQuery: String): FlashcardSet? {
        val geminiApiKey = configRepository.getGeminiApiKey() ?: return null

        return try {
            val agent = createAgent(geminiApiKey)

            val prompt = markdown {
                +"Please generate exactly $count flashcards on the topic of '$topic'"

                h2("Additional Information")
                +"Here is more information to help guide you:"
                +userQuery
            }

            agent.run(prompt)
        } catch (e: Exception) {
            // Return null for any API errors (invalid API key, network issues, etc.)
            println("Failed to generate flashcards: ${e.message}")
            null
        }
    }
}
