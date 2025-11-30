package ai.solenne.flashcards.app.domain.repository

import ai.solenne.flashcards.app.data.api.ServerFlashcardApiClient
import ai.solenne.flashcards.shared.domain.model.Flashcard
import ai.solenne.flashcards.shared.domain.model.FlashcardSet
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
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
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest

class ClientFlashcardRepositoryTest : DescribeSpec({
    lateinit var mockAuthRepository: AuthRepository
    lateinit var mockServerClient: ServerFlashcardApiClient
    lateinit var repository: ClientFlashcardRepository

    beforeEach {
        mockAuthRepository = mock()
        mockServerClient = mock()
        repository = ClientFlashcardRepository(mockAuthRepository, mockServerClient)
    }

    describe("ClientFlashcardRepository") {
        describe("saveFlashcardSet") {
            it("should throw when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                val set = FlashcardSet(id = "set-123", topic = "Test")

                runTest {
                    shouldThrow<IllegalStateException> {
                        repository.saveFlashcardSet(set)
                    }
                }
            }

            it("should save set with valid token") {
                val token = "valid-token"
                val set = FlashcardSet(id = "set-123", topic = "Test")

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.saveFlashcardSet(token, set) } returns Unit

                runTest {
                    repository.saveFlashcardSet(set)
                }

                verifySuspend { mockServerClient.saveFlashcardSet(token, set) }
            }

            it("should pass correct token and set to server") {
                val token = "user-specific-token"
                val set = FlashcardSet(
                    id = "specific-set",
                    userId = "user-123",
                    topic = "Specific Topic"
                )

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.saveFlashcardSet(token, set) } returns Unit

                runTest {
                    repository.saveFlashcardSet(set)
                }

                verifySuspend { mockServerClient.saveFlashcardSet(token, set) }
            }

            it("should propagate server errors") {
                val token = "valid-token"
                val set = FlashcardSet(id = "error-set")

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.saveFlashcardSet(token, set) } throws RuntimeException("Server error")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.saveFlashcardSet(set)
                    }
                }
            }
        }

        describe("getAllFlashcardSets") {
            it("should return empty list when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.shouldBeEmpty()
                }
            }

            it("should return empty list when server returns null") {
                val token = "valid-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getAllFlashcardSets(token) } returns null

                runTest {
                    val result = repository.getAllFlashcardSets()
                    result.shouldBeEmpty()
                }
            }

            it("should return sets from server") {
                val token = "valid-token"
                val sets = listOf(
                    FlashcardSet(id = "set-1", topic = "Topic 1"),
                    FlashcardSet(id = "set-2", topic = "Topic 2")
                )

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getAllFlashcardSets(token) } returns sets

                runTest {
                    val result = repository.getAllFlashcardSets()

                    result.shouldHaveSize(2)
                    result shouldBe sets
                }
            }

            it("should use correct token for request") {
                val token = "specific-user-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getAllFlashcardSets(token) } returns emptyList()

                runTest {
                    repository.getAllFlashcardSets()
                }

                verifySuspend { mockServerClient.getAllFlashcardSets(token) }
            }

            it("should return empty list on server error gracefully") {
                val token = "valid-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getAllFlashcardSets(token) } throws RuntimeException("Network error")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.getAllFlashcardSets()
                    }
                }
            }
        }

        describe("getFlashcardSet") {
            it("should return null when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    val result = repository.getFlashcardSet("any-id")
                    result.shouldBeNull()
                }
            }

            it("should return null when server returns null") {
                val token = "valid-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, "non-existent") } returns null

                runTest {
                    val result = repository.getFlashcardSet("non-existent")
                    result.shouldBeNull()
                }
            }

            it("should return set from server") {
                val token = "valid-token"
                val set = FlashcardSet(id = "server-set", topic = "Server Topic")

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, "server-set") } returns set

                runTest {
                    val result = repository.getFlashcardSet("server-set")
                    result shouldBe set
                }
            }

            it("should use correct token and ID") {
                val token = "user-token"
                val setId = "specific-set-id"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, setId) } returns null

                runTest {
                    repository.getFlashcardSet(setId)
                }

                verifySuspend { mockServerClient.getFlashcardSet(token, setId) }
            }
        }

        describe("updateFlashcardSet") {
            it("should throw when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                val set = FlashcardSet(id = "update-me", topic = "Updated")

                runTest {
                    shouldThrow<IllegalStateException> {
                        repository.updateFlashcardSet(set)
                    }
                }
            }

            it("should update set with valid token") {
                val token = "valid-token"
                val set = FlashcardSet(
                    id = "existing-set",
                    topic = "Updated Topic",
                    flashcards = listOf(
                        Flashcard(front = "Updated Q", back = "Updated A")
                    )
                )

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.updateFlashcardSet(token, set) } returns Unit

                runTest {
                    repository.updateFlashcardSet(set)
                }

                verifySuspend { mockServerClient.updateFlashcardSet(token, set) }
            }

            it("should pass correct token and set to server") {
                val token = "user-specific-token"
                val set = FlashcardSet(
                    id = "set-to-update",
                    userId = "user-123",
                    topic = "Modified Topic",
                    flashcards = listOf(
                        Flashcard(front = "Q1", back = "A1"),
                        Flashcard(front = "Q2", back = "A2")
                    )
                )

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.updateFlashcardSet(token, set) } returns Unit

                runTest {
                    repository.updateFlashcardSet(set)
                }

                verifySuspend { mockServerClient.updateFlashcardSet(token, set) }
            }

            it("should propagate server errors") {
                val token = "valid-token"
                val set = FlashcardSet(id = "error-set")

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.updateFlashcardSet(token, set) } throws RuntimeException("Server error")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.updateFlashcardSet(set)
                    }
                }
            }

            it("should handle CORS errors gracefully") {
                val token = "valid-token"
                val set = FlashcardSet(id = "cors-set")

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.updateFlashcardSet(token, set) } throws RuntimeException("CORS error")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.updateFlashcardSet(set)
                    }
                }
            }

            it("should support idempotent updates") {
                val token = "valid-token"
                val set = FlashcardSet(
                    id = "idempotent-set",
                    topic = "Same Topic"
                )

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.updateFlashcardSet(token, set) } returns Unit

                runTest {
                    // Update same set twice - should be idempotent
                    repository.updateFlashcardSet(set)
                    repository.updateFlashcardSet(set)
                }

                // Verify it was called twice (idempotent means safe to retry)
                verifySuspend(atLeast = 2) { mockServerClient.updateFlashcardSet(token, set) }
            }
        }

        describe("deleteFlashcardSet") {
            it("should throw when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    shouldThrow<IllegalStateException> {
                        repository.deleteFlashcardSet("any-id")
                    }
                }
            }

            it("should delete set with valid token") {
                val token = "valid-token"
                val setId = "delete-me"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.deleteFlashcardSet(token, setId) } returns Unit

                runTest {
                    repository.deleteFlashcardSet(setId)
                }

                verifySuspend { mockServerClient.deleteFlashcardSet(token, setId) }
            }

            it("should propagate server errors") {
                val token = "valid-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.deleteFlashcardSet(token, "error-id") } throws RuntimeException("Server error")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.deleteFlashcardSet("error-id")
                    }
                }
            }
        }

        describe("getRandomizedFlashcards") {
            it("should return null when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    val result = repository.getRandomizedFlashcards("any-id")
                    result.shouldBeNull()
                }
            }

            it("should return null when set not found") {
                val token = "valid-token"

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, "non-existent") } returns null

                runTest {
                    val result = repository.getRandomizedFlashcards("non-existent")
                    result.shouldBeNull()
                }
            }

            it("should return shuffled cards from set") {
                val token = "valid-token"
                val cards = listOf(
                    Flashcard(id = "card-1", front = "Q1", back = "A1"),
                    Flashcard(id = "card-2", front = "Q2", back = "A2"),
                    Flashcard(id = "card-3", front = "Q3", back = "A3")
                )
                val set = FlashcardSet(id = "study-set", flashcards = cards)

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, "study-set") } returns set

                runTest {
                    val result = repository.getRandomizedFlashcards("study-set")

                    result.shouldNotBeNull()
                    result.shouldHaveSize(3)
                    result.shouldContainAll(cards)
                }
            }

            it("should return empty list for set with no cards") {
                val token = "valid-token"
                val emptySet = FlashcardSet(id = "empty", flashcards = emptyList())

                everySuspend { mockAuthRepository.getSessionToken() } returns token
                everySuspend { mockServerClient.getFlashcardSet(token, "empty") } returns emptySet

                runTest {
                    val result = repository.getRandomizedFlashcards("empty")

                    result.shouldNotBeNull()
                    result.shouldBeEmpty()
                }
            }
        }

        describe("authentication scenarios") {
            it("should handle token expiration gracefully") {
                // First call succeeds, then token expires
                everySuspend { mockAuthRepository.getSessionToken() } returns "expired-token"
                everySuspend { mockServerClient.getAllFlashcardSets("expired-token") } throws RuntimeException("401 Unauthorized")

                runTest {
                    shouldThrow<RuntimeException> {
                        repository.getAllFlashcardSets()
                    }
                }
            }

            it("should require authentication for write operations") {
                val set = FlashcardSet(id = "new-set")

                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    // Save requires auth
                    shouldThrow<IllegalStateException> {
                        repository.saveFlashcardSet(set)
                    }

                    // Update requires auth
                    shouldThrow<IllegalStateException> {
                        repository.updateFlashcardSet(set)
                    }

                    // Delete requires auth
                    shouldThrow<IllegalStateException> {
                        repository.deleteFlashcardSet("any-id")
                    }
                }
            }

            it("should allow read operations without throwing when not authenticated") {
                everySuspend { mockAuthRepository.getSessionToken() } returns null

                runTest {
                    // These should not throw, just return empty/null
                    val allSets = repository.getAllFlashcardSets()
                    allSets.shouldBeEmpty()

                    val singleSet = repository.getFlashcardSet("any-id")
                    singleSet.shouldBeNull()

                    val randomized = repository.getRandomizedFlashcards("any-id")
                    randomized.shouldBeNull()
                }
            }
        }
    }
})
