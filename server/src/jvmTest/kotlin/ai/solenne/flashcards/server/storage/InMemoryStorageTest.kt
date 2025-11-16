package ai.solenne.flashcards.server.storage

import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class InMemoryStorageTest : DescribeSpec({
    lateinit var storage: InMemoryStorage

    beforeEach {
        storage = InMemoryStorage()
    }

    describe("InMemoryStorage") {
        describe("save") {
            it("should save a flashcard set") {
                val set = FlashcardSet(
                    id = "set-123",
                    userId = "user-456",
                    topic = "Kotlin Basics"
                )

                storage.save(set)

                val retrieved = storage.getById("set-123")
                retrieved shouldBe set
            }

            it("should overwrite existing set with same ID") {
                val original = FlashcardSet(
                    id = "set-123",
                    topic = "Original Topic"
                )
                storage.save(original)

                val updated = original.copy(topic = "Updated Topic")
                storage.save(updated)

                val retrieved = storage.getById("set-123")
                retrieved?.topic shouldBe "Updated Topic"
            }

            it("should save multiple sets") {
                val set1 = FlashcardSet(id = "set-1", topic = "Topic 1")
                val set2 = FlashcardSet(id = "set-2", topic = "Topic 2")
                val set3 = FlashcardSet(id = "set-3", topic = "Topic 3")

                storage.save(set1)
                storage.save(set2)
                storage.save(set3)

                val all = storage.getAll()
                all.shouldHaveSize(3)
            }

            it("should preserve flashcards in the set") {
                val cards = listOf(
                    Flashcard(id = "card-1", front = "Q1", back = "A1"),
                    Flashcard(id = "card-2", front = "Q2", back = "A2")
                )
                val set = FlashcardSet(
                    id = "set-with-cards",
                    flashcards = cards
                )

                storage.save(set)

                val retrieved = storage.getById("set-with-cards")
                retrieved?.flashcards shouldBe cards
            }
        }

        describe("getAll") {
            it("should return empty list when no sets stored") {
                val result = storage.getAll()
                result.shouldBeEmpty()
            }

            it("should return all saved sets") {
                val set1 = FlashcardSet(id = "set-1", topic = "Topic 1")
                val set2 = FlashcardSet(id = "set-2", topic = "Topic 2")

                storage.save(set1)
                storage.save(set2)

                val all = storage.getAll()
                all.shouldHaveSize(2)
                all.shouldContain(set1)
                all.shouldContain(set2)
            }

            it("should return sets for different users") {
                val userASet = FlashcardSet(id = "set-a", userId = "user-a")
                val userBSet = FlashcardSet(id = "set-b", userId = "user-b")

                storage.save(userASet)
                storage.save(userBSet)

                val all = storage.getAll()
                all.shouldHaveSize(2)
            }

            it("should include anonymous sets") {
                val anonymousSet = FlashcardSet(id = "anon-set", userId = null)
                val userSet = FlashcardSet(id = "user-set", userId = "user-123")

                storage.save(anonymousSet)
                storage.save(userSet)

                val all = storage.getAll()
                all.any { it.userId == null } shouldBe true
                all.any { it.userId == "user-123" } shouldBe true
            }
        }

        describe("getById") {
            it("should return null for non-existent ID") {
                val result = storage.getById("non-existent")
                result.shouldBeNull()
            }

            it("should return the set for valid ID") {
                val set = FlashcardSet(id = "valid-id", topic = "Valid Topic")
                storage.save(set)

                val retrieved = storage.getById("valid-id")
                retrieved shouldBe set
            }

            it("should return correct set when multiple exist") {
                val set1 = FlashcardSet(id = "id-1", topic = "Topic 1")
                val set2 = FlashcardSet(id = "id-2", topic = "Topic 2")
                val set3 = FlashcardSet(id = "id-3", topic = "Topic 3")

                storage.save(set1)
                storage.save(set2)
                storage.save(set3)

                storage.getById("id-2")?.topic shouldBe "Topic 2"
            }

            it("should return null after set is deleted") {
                val set = FlashcardSet(id = "delete-me", topic = "Temporary")
                storage.save(set)
                storage.delete("delete-me")

                storage.getById("delete-me").shouldBeNull()
            }
        }

        describe("delete") {
            it("should remove a set by ID") {
                val set = FlashcardSet(id = "to-delete", topic = "Bye")
                storage.save(set)

                storage.delete("to-delete")

                storage.getById("to-delete").shouldBeNull()
            }

            it("should not affect other sets") {
                val set1 = FlashcardSet(id = "keep-1", topic = "Keep Me 1")
                val set2 = FlashcardSet(id = "delete-me", topic = "Delete Me")
                val set3 = FlashcardSet(id = "keep-2", topic = "Keep Me 2")

                storage.save(set1)
                storage.save(set2)
                storage.save(set3)

                storage.delete("delete-me")

                storage.getById("keep-1").shouldNotBeNull()
                storage.getById("delete-me").shouldBeNull()
                storage.getById("keep-2").shouldNotBeNull()
                storage.getAll().shouldHaveSize(2)
            }

            it("should handle deleting non-existent ID gracefully") {
                // Should not throw
                storage.delete("non-existent-id")
            }

            it("should handle double deletion") {
                val set = FlashcardSet(id = "double-delete")
                storage.save(set)

                storage.delete("double-delete")
                storage.delete("double-delete") // Second delete should not throw

                storage.getById("double-delete").shouldBeNull()
            }
        }

        describe("concurrent access") {
            it("should handle concurrent saves") {
                coroutineScope {
                    val jobs = (1..100).map { i ->
                        launch {
                            val set = FlashcardSet(id = "concurrent-$i", topic = "Topic $i")
                            storage.save(set)
                        }
                    }
                    jobs.forEach { it.join() }
                }

                storage.getAll().shouldHaveSize(100)
            }

            it("should handle concurrent reads") {
                val set = FlashcardSet(id = "shared-set", topic = "Shared")
                storage.save(set)

                coroutineScope {
                    val jobs = (1..100).map {
                        launch {
                            val result = storage.getById("shared-set")
                            result.shouldNotBeNull()
                        }
                    }
                    jobs.forEach { it.join() }
                }
            }

            it("should handle concurrent deletes") {
                // Save multiple sets
                (1..50).forEach { i ->
                    storage.save(FlashcardSet(id = "delete-concurrent-$i"))
                }

                coroutineScope {
                    val jobs = (1..50).map { i ->
                        launch {
                            storage.delete("delete-concurrent-$i")
                        }
                    }
                    jobs.forEach { it.join() }
                }

                storage.getAll().shouldBeEmpty()
            }
        }

        describe("use case scenarios") {
            it("should handle user flashcard set workflow") {
                val userId = "user-123"

                // User creates first set
                val set1 = FlashcardSet(
                    id = "set-1",
                    userId = userId,
                    topic = "First Topic",
                    flashcards = listOf(
                        Flashcard(front = "Q1", back = "A1")
                    )
                )
                storage.save(set1)

                // User creates second set
                val set2 = FlashcardSet(
                    id = "set-2",
                    userId = userId,
                    topic = "Second Topic",
                    flashcards = listOf(
                        Flashcard(front = "Q2", back = "A2")
                    )
                )
                storage.save(set2)

                // User views all their sets
                val userSets = storage.getAll().filter { it.userId == userId }
                userSets.shouldHaveSize(2)

                // User deletes one set
                storage.delete("set-1")

                val remainingSets = storage.getAll().filter { it.userId == userId }
                remainingSets.shouldHaveSize(1)
                remainingSets.first().topic shouldBe "Second Topic"
            }

            it("should handle regenerated flashcard set") {
                val originalSet = FlashcardSet(
                    id = "regenerate-set",
                    topic = "Physics",
                    flashcards = listOf(
                        Flashcard(front = "What is force?", back = "Mass times acceleration")
                    )
                )
                storage.save(originalSet)

                // User regenerates with more cards
                val regeneratedSet = originalSet.copy(
                    flashcards = listOf(
                        Flashcard(front = "What is force?", back = "F = ma"),
                        Flashcard(front = "What is energy?", back = "Capacity to do work"),
                        Flashcard(front = "What is momentum?", back = "Mass times velocity")
                    )
                )
                storage.save(regeneratedSet)

                val retrieved = storage.getById("regenerate-set")
                retrieved?.flashcards?.shouldHaveSize(3)
            }

            it("should handle large flashcard sets") {
                val largeCards = (1..500).map { i ->
                    Flashcard(
                        id = "card-$i",
                        front = "Question $i",
                        back = "Answer $i"
                    )
                }
                val largeSet = FlashcardSet(
                    id = "large-set",
                    flashcards = largeCards
                )

                storage.save(largeSet)

                val retrieved = storage.getById("large-set")
                retrieved?.flashcards?.shouldHaveSize(500)
            }
        }

        describe("edge cases") {
            it("should handle empty flashcard sets") {
                val emptySet = FlashcardSet(
                    id = "empty-set",
                    topic = "Empty",
                    flashcards = emptyList()
                )
                storage.save(emptySet)

                val retrieved = storage.getById("empty-set")
                retrieved?.flashcards?.shouldBeEmpty()
            }

            it("should handle special characters in IDs") {
                val specialId = "set-with-!@#\$%^&*()-_+=[]{}|\\:\";<>?/,."
                val set = FlashcardSet(id = specialId, topic = "Special")
                storage.save(set)

                storage.getById(specialId).shouldNotBeNull()
            }

            it("should handle very long topic names") {
                val longTopic = "A".repeat(10000)
                val set = FlashcardSet(id = "long-topic-set", topic = longTopic)
                storage.save(set)

                val retrieved = storage.getById("long-topic-set")
                retrieved?.topic shouldBe longTopic
            }
        }
    }
})
