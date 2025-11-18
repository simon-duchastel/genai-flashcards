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
class UserTest : DescribeSpec({
    describe("User") {
        describe("default construction") {
            it("should generate a unique userId by default") {
                val user1 = User()
                val user2 = User()

                user1.userId.shouldNotBeEmpty()
                user2.userId.shouldNotBeEmpty()
                user1.userId shouldNotBe user2.userId
            }

            it("should have empty authId by default") {
                val user = User()
                user.authId shouldBe ""
            }

            it("should set createdAt to current timestamp") {
                val before = Clock.System.now().toEpochMilliseconds()
                val user = User()
                val after = Clock.System.now().toEpochMilliseconds()

                user.createdAt shouldBeGreaterThan 0
                user.createdAt shouldBeGreaterThan before - 1000
            }
        }

        describe("custom construction") {
            it("should accept custom userId") {
                val user = User(userId = "custom-user-id")
                user.userId shouldBe "custom-user-id"
            }

            it("should accept Google OAuth authId") {
                val googleAuthId = "google-oauth-sub-123456789"
                val user = User(authId = googleAuthId)
                user.authId shouldBe googleAuthId
            }

            it("should accept Apple OAuth authId") {
                val appleAuthId = "apple-oauth-sub-987654321"
                val user = User(authId = appleAuthId)
                user.authId shouldBe appleAuthId
            }

            it("should accept custom createdAt") {
                val user = User(createdAt = 1234567890L)
                user.createdAt shouldBe 1234567890L
            }
        }

        describe("OAuth scenarios") {
            it("should represent Google authenticated user") {
                val user = User(
                    userId = "internal-uuid-123",
                    authId = "google-sub-claim-value"
                )
                user.userId shouldNotBe user.authId
                user.userId shouldBe "internal-uuid-123"
                user.authId shouldBe "google-sub-claim-value"
            }

            it("should represent Apple authenticated user") {
                val user = User(
                    userId = "internal-uuid-456",
                    authId = "apple-user-identifier"
                )
                user.userId shouldBe "internal-uuid-456"
                user.authId shouldBe "apple-user-identifier"
            }

            it("should allow same internal ID with different auth providers") {
                val user = User(
                    userId = "same-internal-id",
                    authId = "provider-specific-id"
                )
                user.userId shouldNotBe user.authId
            }
        }

        describe("serialization") {
            val json = Json { prettyPrint = false }

            it("should serialize to JSON correctly") {
                val user = User(
                    userId = "user-123",
                    authId = "oauth-456",
                    createdAt = 1234567890L
                )

                val serialized = json.encodeToString(user)
                serialized shouldNotBe ""
            }

            it("should deserialize from JSON correctly") {
                val jsonString = """
                    {
                        "userId": "user-abc",
                        "authId": "google-sub-xyz",
                        "createdAt": 9876543210
                    }
                """.trimIndent()

                val user = json.decodeFromString<User>(jsonString)
                user.userId shouldBe "user-abc"
                user.authId shouldBe "google-sub-xyz"
                user.createdAt shouldBe 9876543210L
            }

            it("should round-trip serialize correctly") {
                val original = User(
                    userId = "round-trip-user",
                    authId = "round-trip-auth",
                    createdAt = 5555555555L
                )

                val serialized = json.encodeToString(original)
                val deserialized = json.decodeFromString<User>(serialized)

                deserialized shouldBe original
            }
        }

        describe("data class equality") {
            it("should be equal when all properties match") {
                val user1 = User(
                    userId = "uid",
                    authId = "aid",
                    createdAt = 100L
                )
                val user2 = User(
                    userId = "uid",
                    authId = "aid",
                    createdAt = 100L
                )

                user1 shouldBe user2
            }

            it("should not be equal when userId differs") {
                val user1 = User(userId = "user1", authId = "same-auth")
                val user2 = User(userId = "user2", authId = "same-auth")

                user1 shouldNotBe user2
            }

            it("should not be equal when authId differs") {
                val user1 = User(userId = "same-user", authId = "auth1")
                val user2 = User(userId = "same-user", authId = "auth2")

                user1 shouldNotBe user2
            }

            it("should not be equal when createdAt differs") {
                val user1 = User(userId = "user", authId = "auth", createdAt = 100L)
                val user2 = User(userId = "user", authId = "auth", createdAt = 200L)

                user1 shouldNotBe user2
            }
        }

        describe("copy operations") {
            it("should allow updating authId") {
                val original = User(
                    userId = "user-id",
                    authId = "old-auth",
                    createdAt = 1000L
                )

                val updated = original.copy(authId = "new-auth")

                updated.userId shouldBe original.userId
                updated.authId shouldBe "new-auth"
                updated.createdAt shouldBe original.createdAt
            }

            it("should preserve createdAt when copying") {
                val original = User(createdAt = 12345L)
                val copied = original.copy(authId = "new-auth")

                copied.createdAt shouldBe 12345L
            }
        }

        describe("hashCode") {
            it("should have same hashCode for equal objects") {
                val user1 = User(
                    userId = "uid",
                    authId = "aid",
                    createdAt = 100L
                )
                val user2 = User(
                    userId = "uid",
                    authId = "aid",
                    createdAt = 100L
                )

                user1.hashCode() shouldBe user2.hashCode()
            }
        }
    }
})
