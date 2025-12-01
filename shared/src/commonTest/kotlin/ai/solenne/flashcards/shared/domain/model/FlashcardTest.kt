package ai.solenne.flashcards.shared.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.longs.shouldBeGreaterThan
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FlashcardTest : DescribeSpec({
    describe("Flashcard") {
        describe("default construction") {
            it("should generate a unique ID by default") {
                val card1 = Flashcard()
                val card2 = Flashcard()

                card1.id.shouldNotBeEmpty()
                card2.id.shouldNotBeEmpty()
                card1.id shouldNotBe card2.id
            }

            it("should have empty setId by default") {
                val card = Flashcard()
                card.setId shouldBe ""
            }

            it("should have empty front by default") {
                val card = Flashcard()
                card.front shouldBe ""
            }

            it("should have empty back by default") {
                val card = Flashcard()
                card.back shouldBe ""
            }

            it("should set createdAt to current timestamp") {
                val before = Clock.System.now().toEpochMilliseconds()
                val card = Flashcard()
                val after = Clock.System.now().toEpochMilliseconds()

                card.createdAt shouldBeGreaterThan 0
                card.createdAt shouldBeGreaterThan before - 1000
            }
        }

        describe("custom construction") {
            it("should accept custom ID") {
                val card = Flashcard(id = "custom-id-123")
                card.id shouldBe "custom-id-123"
            }

            it("should accept custom setId") {
                val card = Flashcard(setId = "set-456")
                card.setId shouldBe "set-456"
            }

            it("should accept custom front and back") {
                val card = Flashcard(
                    front = "What is the capital of France?",
                    back = "Paris"
                )
                card.front shouldBe "What is the capital of France?"
                card.back shouldBe "Paris"
            }

            it("should accept custom createdAt") {
                val card = Flashcard(createdAt = 1234567890L)
                card.createdAt shouldBe 1234567890L
            }
        }

        describe("content validation scenarios") {
            it("should handle long question text") {
                val longQuestion = "A".repeat(10000)
                val card = Flashcard(front = longQuestion, back = "Short answer")
                card.front shouldBe longQuestion
            }

            it("should handle long answer text") {
                val longAnswer = "B".repeat(10000)
                val card = Flashcard(front = "Short question", back = longAnswer)
                card.back shouldBe longAnswer
            }

            it("should handle special characters") {
                val card = Flashcard(
                    front = "What does π represent?",
                    back = "π ≈ 3.14159... (ratio of circle's circumference to diameter)"
                )
                card.front shouldBe "What does π represent?"
                card.back shouldBe "π ≈ 3.14159... (ratio of circle's circumference to diameter)"
            }

            it("should handle multiline content") {
                val multilineFront = """
                    Consider the following code:
                    fun main() {
                        println("Hello")
                    }
                    What does it print?
                """.trimIndent()
                val card = Flashcard(front = multilineFront, back = "Hello")
                card.front shouldBe multilineFront
            }

            it("should handle empty strings for front and back") {
                val card = Flashcard(front = "", back = "")
                card.front shouldBe ""
                card.back shouldBe ""
            }
        }

        describe("serialization") {
            val json = Json { prettyPrint = false }

            it("should serialize to JSON correctly") {
                val card = Flashcard(
                    id = "card-123",
                    setId = "set-456",
                    front = "Question",
                    back = "Answer",
                    createdAt = 1234567890L
                )

                val serialized = json.encodeToString(card)
                serialized shouldNotBe ""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """
                    {
                        "id": "card-abc",
                        "setId": "set-xyz",
                        "front": "What is 2+2?",
                        "back": "4",
                        "createdAt": 9999999999
                    }
                """.trimIndent()

                val card = json.decodeFromString<Flashcard>(jsonString)
                card.id shouldBe "card-abc"
                card.setId shouldBe "set-xyz"
                card.front shouldBe "What is 2+2?"
                card.back shouldBe "4"
                card.createdAt shouldBe 9999999999L
            }

            it("should round-trip serialize correctly") {
                val original = Flashcard(
                    id = "round-trip-id",
                    setId = "round-trip-set",
                    front = "Original Question",
                    back = "Original Answer",
                    createdAt = 1111111111L
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<Flashcard>(serialized)

                deserialized shouldBe original
            }

            it("should handle JSON with escaped characters") {
                val jsonString = """
                    {
                        "id": "card-1",
                        "setId": "set-1",
                        "front": "What is \"JSON\"?",
                        "back": "JavaScript Object Notation\nUsed for data interchange",
                        "createdAt": 1000
                    }
                """.trimIndent()

                val card = json.decodeFromString<Flashcard>(jsonString)
                card.front shouldBe "What is \"JSON\"?"
                card.back shouldBe "JavaScript Object Notation\nUsed for data interchange"
            }
        }

        describe("data class equality") {
            it("should be equal when all properties match") {
                val card1 = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Q",
                    back = "A",
                    createdAt = 100L
                )
                val card2 = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Q",
                    back = "A",
                    createdAt = 100L
                )

                card1 shouldBe card2
            }

            it("should not be equal when IDs differ") {
                val card1 = Flashcard(id = "id1", front = "Q", back = "A")
                val card2 = Flashcard(id = "id2", front = "Q", back = "A")

                card1 shouldNotBe card2
            }

            it("should not be equal when content differs") {
                val card1 = Flashcard(id = "same-id", front = "Q1", back = "A")
                val card2 = Flashcard(id = "same-id", front = "Q2", back = "A")

                card1 shouldNotBe card2
            }
        }

        describe("copy operations") {
            it("should allow updating front while preserving other fields") {
                val original = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Old question",
                    back = "Answer",
                    createdAt = 1000L
                )

                val updated = original.copy(front = "New question")

                updated.id shouldBe original.id
                updated.setId shouldBe original.setId
                updated.front shouldBe "New question"
                updated.back shouldBe original.back
                updated.createdAt shouldBe original.createdAt
            }

            it("should allow updating back while preserving other fields") {
                val original = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Question",
                    back = "Old answer",
                    createdAt = 1000L
                )

                val updated = original.copy(back = "New answer")

                updated.back shouldBe "New answer"
                updated.front shouldBe original.front
            }

            it("should allow reassigning to different set") {
                val card = Flashcard(setId = "old-set", front = "Q", back = "A")
                val reassigned = card.copy(setId = "new-set")

                reassigned.setId shouldBe "new-set"
            }
        }

        describe("hashCode") {
            it("should have same hashCode for equal objects") {
                val card1 = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Q",
                    back = "A",
                    createdAt = 100L
                )
                val card2 = Flashcard(
                    id = "id",
                    setId = "set",
                    front = "Q",
                    back = "A",
                    createdAt = 100L
                )

                card1.hashCode() shouldBe card2.hashCode()
            }
        }
    }
})
