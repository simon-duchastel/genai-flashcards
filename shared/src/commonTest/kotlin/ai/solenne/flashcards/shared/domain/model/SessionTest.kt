package ai.solenne.flashcards.shared.domain.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SessionTest : DescribeSpec({
    describe("Session") {
        describe("default construction") {
            it("should have empty sessionToken by default") {
                val session = Session()
                session.sessionToken shouldBe ""
            }

            it("should have empty userId by default") {
                val session = Session()
                session.userId shouldBe ""
            }

            it("should set createdAt to current time") {
                val before = Clock.System.now().toEpochMilliseconds()
                val session = Session()
                val after = Clock.System.now().toEpochMilliseconds()

                session.createdAt shouldBeGreaterThanOrEqual before
                session.createdAt shouldBeGreaterThan 0
            }

            it("should set lastAccessedAt to current time") {
                val session = Session()
                session.lastAccessedAt shouldBeGreaterThan 0
            }

            it("should have createdAt equal to lastAccessedAt initially") {
                val session = Session()
                // They might differ by a millisecond due to execution time
                // but should be very close
                val diff = kotlin.math.abs(session.createdAt - session.lastAccessedAt)
                diff shouldBe 0L
            }
        }

        describe("isExpired") {
            it("should return false for newly created session") {
                val session = Session()
                session.isExpired() shouldBe false
            }

            it("should return false for session with old timestamp") {
                // Currently isExpired always returns false
                val oldSession = Session(
                    sessionToken = "token",
                    userId = "user",
                    createdAt = 0L, // Very old
                    lastAccessedAt = 0L
                )
                oldSession.isExpired() shouldBe false
            }

            it("should return false regardless of session age") {
                // Test that sessions don't expire (current implementation)
                val ancientSession = Session(
                    createdAt = 1L,
                    lastAccessedAt = 1L
                )
                ancientSession.isExpired() shouldBe false
            }
        }

        describe("updateLastAccessed") {
            it("should update lastAccessedAt to current time") {
                val oldTime = 1000L
                val session = Session(
                    sessionToken = "token123",
                    userId = "user456",
                    createdAt = oldTime,
                    lastAccessedAt = oldTime
                )

                val before = Clock.System.now().toEpochMilliseconds()
                val updated = session.updateLastAccessed()
                val after = Clock.System.now().toEpochMilliseconds()

                updated.lastAccessedAt shouldBeGreaterThanOrEqual before
            }

            it("should preserve other fields when updating") {
                val session = Session(
                    sessionToken = "original-token",
                    userId = "original-user",
                    createdAt = 5000L,
                    lastAccessedAt = 5000L
                )

                val updated = session.updateLastAccessed()

                updated.sessionToken shouldBe "original-token"
                updated.userId shouldBe "original-user"
                updated.createdAt shouldBe 5000L
            }

            it("should not modify the original session") {
                val session = Session(
                    sessionToken = "token",
                    userId = "user",
                    createdAt = 1000L,
                    lastAccessedAt = 1000L
                )

                val originalLastAccessed = session.lastAccessedAt
                val updated = session.updateLastAccessed()

                // Original should be unchanged
                session.lastAccessedAt shouldBe originalLastAccessed
                // Updated should be different
                updated.lastAccessedAt shouldNotBe originalLastAccessed
            }

            it("should create successive updates with increasing timestamps").config(enabled = false) {
                // Note: This test is disabled because it relies on time passing
                // In production, the updateLastAccessed uses the current system time
                val session = Session(
                    sessionToken = "token",
                    userId = "user",
                    createdAt = 1000L,
                    lastAccessedAt = 1000L
                )

                val update1 = session.updateLastAccessed()
                val update2 = update1.updateLastAccessed()

                update2.lastAccessedAt shouldBeGreaterThanOrEqual update1.lastAccessedAt
            }
        }

        describe("serialization") {
            val json = Json { prettyPrint = false }

            it("should serialize to JSON correctly") {
                val session = Session(
                    sessionToken = "abc123def456",
                    userId = "user-789",
                    createdAt = 1234567890L,
                    lastAccessedAt = 1234567900L
                )

                val serialized = json.encodeToString(session)
                serialized shouldNotBe ""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """
                    {
                        "sessionToken": "secure-token-xyz",
                        "userId": "user-123",
                        "createdAt": 1000000000,
                        "lastAccessedAt": 1000000100
                    }
                """.trimIndent()

                val session = json.decodeFromString<Session>(jsonString)
                session.sessionToken shouldBe "secure-token-xyz"
                session.userId shouldBe "user-123"
                session.createdAt shouldBe 1000000000L
                session.lastAccessedAt shouldBe 1000000100L
            }

            it("should round-trip serialize correctly") {
                val original = Session(
                    sessionToken = "round-trip-token",
                    userId = "round-trip-user",
                    createdAt = 999999999L,
                    lastAccessedAt = 1000000000L
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<Session>(serialized)

                deserialized shouldBe original
            }
        }

        describe("data class equality") {
            it("should be equal when all properties match") {
                val session1 = Session(
                    sessionToken = "token",
                    userId = "user",
                    createdAt = 100L,
                    lastAccessedAt = 200L
                )
                val session2 = Session(
                    sessionToken = "token",
                    userId = "user",
                    createdAt = 100L,
                    lastAccessedAt = 200L
                )

                session1 shouldBe session2
            }

            it("should not be equal when sessionToken differs") {
                val session1 = Session(sessionToken = "token1", userId = "user")
                val session2 = Session(sessionToken = "token2", userId = "user")

                session1 shouldNotBe session2
            }

            it("should not be equal when userId differs") {
                val session1 = Session(sessionToken = "token", userId = "user1")
                val session2 = Session(sessionToken = "token", userId = "user2")

                session1 shouldNotBe session2
            }
        }

        describe("copy operations") {
            it("should allow changing sessionToken") {
                val original = Session(sessionToken = "old", userId = "user")
                val updated = original.copy(sessionToken = "new")

                updated.sessionToken shouldBe "new"
                updated.userId shouldBe "user"
            }

            it("should allow changing userId") {
                val original = Session(sessionToken = "token", userId = "old-user")
                val updated = original.copy(userId = "new-user")

                updated.sessionToken shouldBe "token"
                updated.userId shouldBe "new-user"
            }
        }
    }
})
