package ai.solenne.flashcards.shared.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FlashcardSetTest : DescribeSpec({
    describe("FlashcardSet") {
        describe("default construction") {
            it("should generate a unique ID by default") {
                val set1 = FlashcardSet()
                val set2 = FlashcardSet()

                set1.id.shouldNotBeEmpty()
                set2.id.shouldNotBeEmpty()
                set1.id shouldNotBe set2.id
            }

            it("should have null userId by default") {
                val set = FlashcardSet()
                set.userId shouldBe null
            }

            it("should have empty topic by default") {
                val set = FlashcardSet()
                set.topic shouldBe ""
            }

            it("should have empty flashcards list by default") {
                val set = FlashcardSet()
                set.flashcards.shouldBeEmpty()
            }

            it("should set createdAt to current timestamp") {
                val before = Clock.System.now().toEpochMilliseconds()
                val set = FlashcardSet()
                val after = Clock.System.now().toEpochMilliseconds()

                set.createdAt shouldBeGreaterThan 0
                set.createdAt shouldBeGreaterThan before - 1000 // Allow 1 second tolerance
            }
        }

        describe("cardCount computed property") {
            it("should return 0 for empty flashcards") {
                val set = FlashcardSet(flashcards = emptyList())
                set.cardCount shouldBe 0
            }

            it("should return correct count for single flashcard") {
                val card = Flashcard(front = "Q1", back = "A1")
                val set = FlashcardSet(flashcards = listOf(card))
                set.cardCount shouldBe 1
            }

            it("should return correct count for multiple flashcards") {
                val cards = listOf(
                    Flashcard(front = "Q1", back = "A1"),
                    Flashcard(front = "Q2", back = "A2"),
                    Flashcard(front = "Q3", back = "A3")
                )
                val set = FlashcardSet(flashcards = cards)
                set.cardCount shouldBe 3
            }

            it("should update when flashcards are copied with new list") {
                val initialCards = listOf(Flashcard(front = "Q1", back = "A1"))
                val set = FlashcardSet(flashcards = initialCards)
                set.cardCount shouldBe 1

                val newCards = initialCards + Flashcard(front = "Q2", back = "A2")
                val updatedSet = set.copy(flashcards = newCards)
                updatedSet.cardCount shouldBe 2
            }
        }

        describe("serialization") {
            val json = Json { prettyPrint = false }

            it("should serialize to JSON correctly") {
                val card = Flashcard(
                    id = "card-123",
                    setId = "set-456",
                    front = "What is Kotlin?",
                    back = "A modern programming language",
                    createdAt = 1234567890L
                )
                val set = FlashcardSet(
                    id = "set-456",
                    userId = "user-789",
                    topic = "Programming",
                    flashcards = listOf(card),
                    createdAt = 1234567890L
                )

                val serialized = json.encodeToString(set)
                serialized shouldNotBe ""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """
                    {
                        "id": "set-456",
                        "userId": "user-789",
                        "topic": "Programming",
                        "flashcards": [
                            {
                                "id": "card-123",
                                "setId": "set-456",
                                "front": "What is Kotlin?",
                                "back": "A modern programming language",
                                "createdAt": 1234567890
                            }
                        ],
                        "createdAt": 1234567890
                    }
                """.trimIndent()

                val set = json.decodeFromString<FlashcardSet>(jsonString)
                set.id shouldBe "set-456"
                set.userId shouldBe "user-789"
                set.topic shouldBe "Programming"
                set.flashcards.shouldHaveSize(1)
                set.flashcards[0].front shouldBe "What is Kotlin?"
                set.createdAt shouldBe 1234567890L
            }

            it("should handle null userId in serialization") {
                val set = FlashcardSet(
                    id = "set-123",
                    userId = null,
                    topic = "Test",
                    createdAt = 1234567890L
                )

                val serialized = json.encodeToString(set)
                val deserialized = json.decodeFromString<FlashcardSet>(serialized)
                deserialized.userId shouldBe null
            }

            it("should round-trip serialize correctly") {
                val original = FlashcardSet(
                    id = "test-id",
                    userId = "user-id",
                    topic = "Mathematics",
                    flashcards = listOf(
                        Flashcard(
                            id = "card-1",
                            setId = "test-id",
                            front = "2 + 2",
                            back = "4",
                            createdAt = 1000L
                        )
                    ),
                    createdAt = 2000L
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<FlashcardSet>(serialized)

                deserialized shouldBe original
            }
        }

        describe("copy operations") {
            it("should allow updating topic while preserving other fields") {
                val original = FlashcardSet(
                    id = "set-1",
                    userId = "user-1",
                    topic = "Old Topic",
                    createdAt = 1000L
                )

                val updated = original.copy(topic = "New Topic")

                updated.id shouldBe original.id
                updated.userId shouldBe original.userId
                updated.topic shouldBe "New Topic"
                updated.createdAt shouldBe original.createdAt
            }

            it("should allow assigning userId to anonymous set") {
                val anonymousSet = FlashcardSet(userId = null, topic = "Anonymous Set")
                val assignedSet = anonymousSet.copy(userId = "new-user-123")

                assignedSet.userId shouldBe "new-user-123"
            }
        }

        describe("data class equality") {
            it("should be equal when all properties match") {
                val card = Flashcard(
                    id = "c1",
                    setId = "s1",
                    front = "Q",
                    back = "A",
                    createdAt = 100L
                )
                val set1 = FlashcardSet(
                    id = "s1",
                    userId = "u1",
                    topic = "T",
                    flashcards = listOf(card),
                    createdAt = 200L
                )
                val set2 = FlashcardSet(
                    id = "s1",
                    userId = "u1",
                    topic = "T",
                    flashcards = listOf(card),
                    createdAt = 200L
                )

                set1 shouldBe set2
            }

            it("should not be equal when IDs differ") {
                val set1 = FlashcardSet(id = "id1", topic = "Same")
                val set2 = FlashcardSet(id = "id2", topic = "Same")

                set1 shouldNotBe set2
            }
        }
    }
})
