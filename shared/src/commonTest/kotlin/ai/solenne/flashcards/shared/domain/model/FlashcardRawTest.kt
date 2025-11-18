package ai.solenne.flashcards.shared.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class FlashcardRawTest : DescribeSpec({
    describe("FlashcardRaw") {
        describe("construction") {
            it("should require both front and back") {
                val raw = FlashcardRaw(
                    front = "Question",
                    back = "Answer"
                )
                raw.front shouldBe "Question"
                raw.back shouldBe "Answer"
            }

            it("should handle empty strings") {
                val raw = FlashcardRaw(front = "", back = "")
                raw.front shouldBe ""
                raw.back shouldBe ""
            }

            it("should handle special characters") {
                val raw = FlashcardRaw(
                    front = "∫x²dx = ?",
                    back = "x³/3 + C"
                )
                raw.front shouldBe "∫x²dx = ?"
                raw.back shouldBe "x³/3 + C"
            }
        }

        describe("serialization") {
            val json = Json { prettyPrint = false }

            it("should serialize to JSON correctly") {
                val raw = FlashcardRaw(
                    front = "What is Kotlin?",
                    back = "A modern programming language"
                )

                val serialized = json.encodeToString(raw)
                serialized shouldBe """{"front":"What is Kotlin?","back":"A modern programming language"}"""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """{"front":"Q","back":"A"}"""
                val raw = json.decodeFromString<FlashcardRaw>(jsonString)

                raw.front shouldBe "Q"
                raw.back shouldBe "A"
            }

            it("should round-trip serialize correctly") {
                val original = FlashcardRaw(
                    front = "Complex \"question\" with\nnewlines",
                    back = "Complex answer"
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<FlashcardRaw>(serialized)

                deserialized shouldBe original
            }
        }

        describe("data class equality") {
            it("should be equal when content matches") {
                val raw1 = FlashcardRaw(front = "Q", back = "A")
                val raw2 = FlashcardRaw(front = "Q", back = "A")

                raw1 shouldBe raw2
            }

            it("should not be equal when front differs") {
                val raw1 = FlashcardRaw(front = "Q1", back = "A")
                val raw2 = FlashcardRaw(front = "Q2", back = "A")

                raw1 shouldNotBe raw2
            }

            it("should not be equal when back differs") {
                val raw1 = FlashcardRaw(front = "Q", back = "A1")
                val raw2 = FlashcardRaw(front = "Q", back = "A2")

                raw1 shouldNotBe raw2
            }
        }

        describe("copy operations") {
            it("should allow updating front") {
                val original = FlashcardRaw(front = "Old", back = "Answer")
                val updated = original.copy(front = "New")

                updated.front shouldBe "New"
                updated.back shouldBe "Answer"
            }

            it("should allow updating back") {
                val original = FlashcardRaw(front = "Question", back = "Old")
                val updated = original.copy(back = "New")

                updated.front shouldBe "Question"
                updated.back shouldBe "New"
            }
        }
    }

    describe("FlashcardSetRaw") {
        describe("construction") {
            it("should require topic and flashcards") {
                val cards = listOf(
                    FlashcardRaw(front = "Q1", back = "A1"),
                    FlashcardRaw(front = "Q2", back = "A2")
                )
                val setRaw = FlashcardSetRaw(
                    topic = "Mathematics",
                    flashcards = cards
                )

                setRaw.topic shouldBe "Mathematics"
                setRaw.flashcards.shouldHaveSize(2)
            }

            it("should handle empty flashcards list") {
                val setRaw = FlashcardSetRaw(
                    topic = "Empty Set",
                    flashcards = emptyList()
                )

                setRaw.topic shouldBe "Empty Set"
                setRaw.flashcards.shouldHaveSize(0)
            }
        }

        describe("serialization") {
            val json = Json {
                prettyPrint = false
                ignoreUnknownKeys = true
            }

            it("should serialize to JSON correctly") {
                val setRaw = FlashcardSetRaw(
                    topic = "Programming",
                    flashcards = listOf(
                        FlashcardRaw(front = "What is a variable?", back = "A named storage location")
                    )
                )

                val serialized = json.encodeToString(setRaw)
                serialized shouldNotBe ""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """
                    {
                        "topic": "Science",
                        "flashcards": [
                            {"front": "H2O", "back": "Water"},
                            {"front": "CO2", "back": "Carbon Dioxide"}
                        ]
                    }
                """.trimIndent()

                val setRaw = json.decodeFromString<FlashcardSetRaw>(jsonString)
                setRaw.topic shouldBe "Science"
                setRaw.flashcards.shouldHaveSize(2)
                setRaw.flashcards[0].front shouldBe "H2O"
                setRaw.flashcards[1].back shouldBe "Carbon Dioxide"
            }

            it("should round-trip serialize correctly") {
                val original = FlashcardSetRaw(
                    topic = "History",
                    flashcards = listOf(
                        FlashcardRaw(front = "Year of French Revolution", back = "1789"),
                        FlashcardRaw(front = "First US President", back = "George Washington")
                    )
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<FlashcardSetRaw>(serialized)

                deserialized shouldBe original
            }

            it("should handle typical AI response structure") {
                // This simulates what the AI might return
                val aiResponse = """
                    {
                        "topic": "Kotlin Basics",
                        "flashcards": [
                            {
                                "front": "What keyword declares an immutable variable?",
                                "back": "val"
                            },
                            {
                                "front": "What keyword declares a mutable variable?",
                                "back": "var"
                            },
                            {
                                "front": "What is the null-safe call operator?",
                                "back": "?."
                            }
                        ]
                    }
                """.trimIndent()

                val setRaw = json.decodeFromString<FlashcardSetRaw>(aiResponse)
                setRaw.topic shouldBe "Kotlin Basics"
                setRaw.flashcards.shouldHaveSize(3)
            }
        }

        describe("data class equality") {
            it("should be equal when all properties match") {
                val cards = listOf(FlashcardRaw(front = "Q", back = "A"))
                val set1 = FlashcardSetRaw(topic = "T", flashcards = cards)
                val set2 = FlashcardSetRaw(topic = "T", flashcards = cards)

                set1 shouldBe set2
            }

            it("should not be equal when topics differ") {
                val cards = listOf(FlashcardRaw(front = "Q", back = "A"))
                val set1 = FlashcardSetRaw(topic = "T1", flashcards = cards)
                val set2 = FlashcardSetRaw(topic = "T2", flashcards = cards)

                set1 shouldNotBe set2
            }

            it("should not be equal when flashcards differ") {
                val set1 = FlashcardSetRaw(
                    topic = "Same",
                    flashcards = listOf(FlashcardRaw(front = "Q1", back = "A1"))
                )
                val set2 = FlashcardSetRaw(
                    topic = "Same",
                    flashcards = listOf(FlashcardRaw(front = "Q2", back = "A2"))
                )

                set1 shouldNotBe set2
            }
        }

        describe("AI generation scenarios") {
            it("should handle large flashcard sets") {
                val cards = (1..100).map { i ->
                    FlashcardRaw(front = "Question $i", back = "Answer $i")
                }
                val setRaw = FlashcardSetRaw(topic = "Large Set", flashcards = cards)

                setRaw.flashcards.shouldHaveSize(100)
                setRaw.flashcards.last().front shouldBe "Question 100"
            }

            it("should preserve order of flashcards") {
                val cards = listOf(
                    FlashcardRaw(front = "First", back = "1"),
                    FlashcardRaw(front = "Second", back = "2"),
                    FlashcardRaw(front = "Third", back = "3")
                )
                val setRaw = FlashcardSetRaw(topic = "Ordered", flashcards = cards)

                setRaw.flashcards[0].front shouldBe "First"
                setRaw.flashcards[1].front shouldBe "Second"
                setRaw.flashcards[2].front shouldBe "Third"
            }
        }
    }
})
