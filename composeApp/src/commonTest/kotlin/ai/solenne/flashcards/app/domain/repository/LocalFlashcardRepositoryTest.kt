package ai.solenne.flashcards.app.domain.repository

import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import ai.solenne.flashcards.shared.domain.storage.Storage
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.test.runTest

class LocalFlashcardRepositoryTest : DescribeSpec({
    lateinit var mockStorage: Storage
    lateinit var repository: LocalFlashcardRepository

    beforeEach {
        mockStorage = mock()
        repository = LocalFlashcardRepository(mockStorage)
    }

    describe("LocalFlashcardRepository") {
        describe("saveFlashcardSet") {
            it("should delegate save to storage") {
                val set = FlashcardSet(
                    id = "set-123",
                    topic = "Kotlin Basics"
                )

                everySuspend { mockStorage.save(set) } returns Unit

                runTest {
                    repository.saveFlashcardSet(set)
                }

                verifySuspend { mockStorage.save(set) }
            }

            it("should save set with flashcards") {
                val cards = listOf(
                    Flashcard(front = "Q1", back = "A1"),
                    Flashcard(front = "Q2", back = "A2")
                )
                val set = FlashcardSet(
                    id = "set-with-cards",
                    flashcards = cards
                )

                everySuspend { mockStorage.save(set) } returns Unit

                runTest {
                    repository.saveFlashcardSet(set)
                }

                verifySuspend { mockStorage.save(set) }
            }
        }

        describe("getAllFlashcardSets") {
            it("should return empty list when no sets stored") {
                everySuspend { mockStorage.getAll() } returns emptyList()

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.shouldBeEmpty()
                }
            }

            it("should return sets sorted by createdAt descending") {
                val oldSet = FlashcardSet(id = "old", topic = "Old", createdAt = 1000L)
                val middleSet = FlashcardSet(id = "middle", topic = "Middle", createdAt = 2000L)
                val newSet = FlashcardSet(id = "new", topic = "New", createdAt = 3000L)

                // Storage returns in random order
                everySuspend { mockStorage.getAll() } returns listOf(middleSet, oldSet, newSet)

                runTest {
                    val result = repository.getAllFlashcardSets()

                    result.shouldHaveSize(3)
                    // Should be sorted newest first
                    result[0].id shouldBe "new"
                    result[1].id shouldBe "middle"
                    result[2].id shouldBe "old"
                }
            }

            it("should handle sets with same timestamp") {
                val set1 = FlashcardSet(id = "set-1", createdAt = 1000L)
                val set2 = FlashcardSet(id = "set-2", createdAt = 1000L)

                everySuspend { mockStorage.getAll() } returns listOf(set1, set2)

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.shouldHaveSize(2)
                }
            }

            it("should preserve all set data when returning") {
                val set = FlashcardSet(
                    id = "full-set",
                    userId = "user-123",
                    topic = "Complete Set",
                    flashcards = listOf(
                        Flashcard(front = "Q1", back = "A1")
                    ),
                    createdAt = 1000L
                )

                everySuspend { mockStorage.getAll() } returns listOf(set)

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.first() shouldBe set
                }
            }
        }

        describe("getFlashcardSet") {
            it("should return null for non-existent set") {
                everySuspend { mockStorage.getById("non-existent") } returns null

                runTest {
                    val result = repository.getFlashcardSet("non-existent")
                    result.shouldBeNull()
                }
            }

            it("should return set for valid ID") {
                val set = FlashcardSet(id = "valid-id", topic = "Valid")

                everySuspend { mockStorage.getById("valid-id") } returns set

                runTest {
                    val result = repository.getFlashcardSet("valid-id")
                    result shouldBe set
                }
            }

            it("should delegate to storage with correct ID") {
                val setId = "specific-set-id"

                everySuspend { mockStorage.getById(setId) } returns null

                runTest {
                    repository.getFlashcardSet(setId)
                }

                verifySuspend { mockStorage.getById(setId) }
            }
        }

        describe("deleteFlashcardSet") {
            it("should delegate delete to storage") {
                val setId = "delete-me"

                everySuspend { mockStorage.delete(setId) } returns Unit

                runTest {
                    repository.deleteFlashcardSet(setId)
                }

                verifySuspend { mockStorage.delete(setId) }
            }

            it("should pass correct ID to storage") {
                val setId = "specific-id-to-delete"

                everySuspend { mockStorage.delete(setId) } returns Unit

                runTest {
                    repository.deleteFlashcardSet(setId)
                }

                verifySuspend { mockStorage.delete(setId) }
            }
        }

        describe("getRandomizedFlashcards") {
            it("should return null for non-existent set") {
                everySuspend { mockStorage.getById("non-existent") } returns null

                runTest {
                    val result = repository.getRandomizedFlashcards("non-existent")
                    result.shouldBeNull()
                }
            }

            it("should return empty list for set with no cards") {
                val emptySet = FlashcardSet(
                    id = "empty-set",
                    flashcards = emptyList()
                )

                everySuspend { mockStorage.getById("empty-set") } returns emptySet

                runTest {
                    val result = repository.getRandomizedFlashcards("empty-set")
                    result.shouldNotBeNull()
                    result.shouldBeEmpty()
                }
            }

            it("should return all cards from the set") {
                val cards = listOf(
                    Flashcard(id = "card-1", front = "Q1", back = "A1"),
                    Flashcard(id = "card-2", front = "Q2", back = "A2"),
                    Flashcard(id = "card-3", front = "Q3", back = "A3")
                )
                val set = FlashcardSet(id = "study-set", flashcards = cards)

                everySuspend { mockStorage.getById("study-set") } returns set

                runTest {
                    val result = repository.getRandomizedFlashcards("study-set")

                    result.shouldNotBeNull()
                    result.shouldHaveSize(3)
                    // Should contain all the same cards (just possibly reordered)
                    result.shouldContainAll(cards)
                }
            }

            it("should shuffle the cards (randomization check)") {
                val cards = (1..10).map { i ->
                    Flashcard(id = "card-$i", front = "Q$i", back = "A$i")
                }
                val set = FlashcardSet(id = "shuffle-set", flashcards = cards)

                everySuspend { mockStorage.getById("shuffle-set") } returns set

                runTest {
                    // Call multiple times and check if order varies
                    val results = (1..10).map {
                        repository.getRandomizedFlashcards("shuffle-set")!!
                    }

                    // At least some should be in different order
                    // (Note: This is probabilistic but highly likely to pass)
                    val uniqueOrders = results.map { it.map { card -> card.id } }.distinct()
                    // With 10 cards shuffled 10 times, we should get multiple orders
                    uniqueOrders.size shouldNotBe 1
                }
            }

            it("should handle single card set") {
                val singleCard = Flashcard(id = "only", front = "Only Q", back = "Only A")
                val set = FlashcardSet(id = "single", flashcards = listOf(singleCard))

                everySuspend { mockStorage.getById("single") } returns set

                runTest {
                    val result = repository.getRandomizedFlashcards("single")

                    result.shouldNotBeNull()
                    result.shouldHaveSize(1)
                    result.first() shouldBe singleCard
                }
            }
        }

        describe("offline/anonymous use cases") {
            it("should work without userId (anonymous user)") {
                val anonymousSet = FlashcardSet(
                    id = "anon-set",
                    userId = null,
                    topic = "Anonymous Topic"
                )

                everySuspend { mockStorage.save(anonymousSet) } returns Unit

                runTest {
                    repository.saveFlashcardSet(anonymousSet)
                }

                verifySuspend { mockStorage.save(anonymousSet) }
            }

            it("should retrieve anonymous sets") {
                val anonymousSet = FlashcardSet(userId = null, topic = "Anonymous")

                everySuspend { mockStorage.getAll() } returns listOf(anonymousSet)

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.first().userId shouldBe null
                }
            }
        }
    }
})
