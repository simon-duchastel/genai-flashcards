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
import domain.model.FlashcardSet
import domain.repository.FlashcardGenerator

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
     */
    private fun createAgent(apiKey: String): AIAgent<String, FlashcardSet?> {
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

            agent.run(prompt)
        } catch (e: Exception) {
            println("Failed to generate flashcards: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
