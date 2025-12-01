package ai.solenne.flashcards.server.repository

import ai.solenne.flashcards.shared.domain.model.Session
import ai.solenne.flashcards.shared.domain.model.User
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class InMemoryAuthRepositoryTest : DescribeSpec({
    lateinit var repository: InMemoryAuthRepository

    beforeEach {
        repository = InMemoryAuthRepository()
    }

    describe("InMemoryAuthRepository") {
        describe("createSession") {
            it("should create a session with a valid token") {
                val session = repository.createSession("user-123")

                session.sessionToken.shouldNotBeEmpty()
                session.userId shouldBe "user-123"
            }

            it("should create unique sessions for each call") {
                val session1 = repository.createSession("user-123")
                val session2 = repository.createSession("user-123")

                session1.sessionToken shouldNotBe session2.sessionToken
            }

            it("should set createdAt and lastAccessedAt") {
                val session = repository.createSession("user-123")

                session.createdAt shouldBeGreaterThan 0
                session.lastAccessedAt shouldBeGreaterThan 0
            }

            it("should store the session for retrieval") {
                val session = repository.createSession("user-123")
                val retrieved = repository.getSession(session.sessionToken)

                retrieved shouldBe session
            }
        }

        describe("getSession") {
            it("should return null for non-existent token") {
                val result = repository.getSession("non-existent-token")
                result.shouldBeNull()
            }

            it("should return the session for valid token") {
                val created = repository.createSession("user-123")
                val retrieved = repository.getSession(created.sessionToken)

                retrieved.shouldNotBeNull()
                retrieved.userId shouldBe "user-123"
            }

            it("should return null after session is invalidated") {
                val session = repository.createSession("user-123")
                repository.invalidateSession(session.sessionToken)

                val retrieved = repository.getSession(session.sessionToken)
                retrieved.shouldBeNull()
            }
        }

        describe("invalidateSession") {
            it("should remove the session") {
                val session = repository.createSession("user-123")
                repository.invalidateSession(session.sessionToken)

                repository.getSession(session.sessionToken).shouldBeNull()
            }

            it("should not affect other sessions") {
                val session1 = repository.createSession("user-123")
                val session2 = repository.createSession("user-123")

                repository.invalidateSession(session1.sessionToken)

                repository.getSession(session1.sessionToken).shouldBeNull()
                repository.getSession(session2.sessionToken).shouldNotBeNull()
            }

            it("should handle non-existent token gracefully") {
                // Should not throw
                repository.invalidateSession("non-existent-token")
            }
        }

        describe("createUser") {
            it("should store the user") {
                val user = User(
                    userId = "user-123",
                    authId = "google-sub-456"
                )

                val created = repository.createUser(user)

                created shouldBe user
            }

            it("should make user retrievable by userId") {
                val user = User(
                    userId = "user-123",
                    authId = "google-sub-456"
                )

                repository.createUser(user)
                val retrieved = repository.getUserById("user-123")

                retrieved shouldBe user
            }

            it("should make user retrievable by authId") {
                val user = User(
                    userId = "user-123",
                    authId = "google-sub-456"
                )

                repository.createUser(user)
                val retrieved = repository.getUserByAuthId("google-sub-456")

                retrieved shouldBe user
            }

            it("should handle multiple users") {
                val user1 = User(userId = "user-1", authId = "auth-1")
                val user2 = User(userId = "user-2", authId = "auth-2")

                repository.createUser(user1)
                repository.createUser(user2)

                repository.getUserById("user-1") shouldBe user1
                repository.getUserById("user-2") shouldBe user2
            }
        }

        describe("getUserById") {
            it("should return null for non-existent user") {
                val result = repository.getUserById("non-existent")
                result.shouldBeNull()
            }

            it("should return the user for valid userId") {
                val user = User(userId = "user-123", authId = "auth-456")
                repository.createUser(user)

                val retrieved = repository.getUserById("user-123")
                retrieved shouldBe user
            }
        }

        describe("getUserByAuthId") {
            it("should return null for non-existent authId") {
                val result = repository.getUserByAuthId("non-existent")
                result.shouldBeNull()
            }

            it("should return the user for valid authId") {
                val user = User(userId = "user-123", authId = "google-sub-789")
                repository.createUser(user)

                val retrieved = repository.getUserByAuthId("google-sub-789")
                retrieved shouldBe user
            }

            it("should distinguish between different OAuth providers") {
                val googleUser = User(userId = "user-1", authId = "google-sub-123")
                val appleUser = User(userId = "user-2", authId = "apple-id-456")

                repository.createUser(googleUser)
                repository.createUser(appleUser)

                repository.getUserByAuthId("google-sub-123") shouldBe googleUser
                repository.getUserByAuthId("apple-id-456") shouldBe appleUser
            }
        }

        describe("updateSessionAccess") {
            it("should update lastAccessedAt timestamp") {
                val session = repository.createSession("user-123")
                val originalLastAccessed = session.lastAccessedAt

                // Small delay to ensure timestamp changes
                Thread.sleep(1)

                repository.updateSessionAccess(session.sessionToken)
                val updated = repository.getSession(session.sessionToken)

                updated.shouldNotBeNull()
                updated.lastAccessedAt shouldBeGreaterThan originalLastAccessed
            }

            it("should preserve other session fields") {
                val session = repository.createSession("user-123")

                repository.updateSessionAccess(session.sessionToken)
                val updated = repository.getSession(session.sessionToken)

                updated.shouldNotBeNull()
                updated.sessionToken shouldBe session.sessionToken
                updated.userId shouldBe session.userId
                updated.createdAt shouldBe session.createdAt
            }

            it("should handle non-existent token gracefully") {
                // Should not throw
                repository.updateSessionAccess("non-existent-token")
            }
        }

        describe("deleteUserAccount") {
            it("should remove the user") {
                val user = User(userId = "user-123", authId = "auth-456")
                repository.createUser(user)

                repository.deleteUserAccount("user-123")

                repository.getUserById("user-123").shouldBeNull()
            }

            it("should remove the authId mapping") {
                val user = User(userId = "user-123", authId = "auth-456")
                repository.createUser(user)

                repository.deleteUserAccount("user-123")

                repository.getUserByAuthId("auth-456").shouldBeNull()
            }

            it("should remove all sessions for the user") {
                val user = User(userId = "user-123", authId = "auth-456")
                repository.createUser(user)

                val session1 = repository.createSession("user-123")
                val session2 = repository.createSession("user-123")

                repository.deleteUserAccount("user-123")

                repository.getSession(session1.sessionToken).shouldBeNull()
                repository.getSession(session2.sessionToken).shouldBeNull()
            }

            it("should not affect other users") {
                val user1 = User(userId = "user-1", authId = "auth-1")
                val user2 = User(userId = "user-2", authId = "auth-2")

                repository.createUser(user1)
                repository.createUser(user2)

                val session1 = repository.createSession("user-1")
                val session2 = repository.createSession("user-2")

                repository.deleteUserAccount("user-1")

                repository.getUserById("user-2") shouldBe user2
                repository.getSession(session2.sessionToken).shouldNotBeNull()
            }

            it("should handle non-existent user gracefully") {
                // Should not throw
                repository.deleteUserAccount("non-existent-user")
            }
        }

        describe("concurrent access") {
            it("should handle concurrent session creation") {
                coroutineScope {
                    val sessions = (1..100).map { i ->
                        launch {
                            repository.createSession("user-$i")
                        }
                    }
                    sessions.forEach { it.join() }
                }

                // All sessions should be unique and accessible
                // This is a basic concurrency test
            }

            it("should handle concurrent user creation") {
                coroutineScope {
                    val users = (1..50).map { i ->
                        launch {
                            val user = User(userId = "user-$i", authId = "auth-$i")
                            repository.createUser(user)
                        }
                    }
                    users.forEach { it.join() }
                }

                // All users should be accessible
                (1..50).forEach { i ->
                    repository.getUserById("user-$i").shouldNotBeNull()
                }
            }
        }

        describe("OAuth flow scenarios") {
            it("should handle complete OAuth login flow") {
                // 1. Check if user exists (first-time login)
                val existingUser = repository.getUserByAuthId("google-sub-new-user")
                existingUser.shouldBeNull()

                // 2. Create new user
                val newUser = User(
                    userId = "internal-user-id",
                    authId = "google-sub-new-user"
                )
                repository.createUser(newUser)

                // 3. Create session
                val session = repository.createSession(newUser.userId)

                // 4. Validate session exists
                repository.getSession(session.sessionToken).shouldNotBeNull()
            }

            it("should handle returning user login") {
                // Setup: User already exists
                val existingUser = User(
                    userId = "returning-user",
                    authId = "google-sub-returning"
                )
                repository.createUser(existingUser)

                // 1. Check if user exists
                val user = repository.getUserByAuthId("google-sub-returning")
                user.shouldNotBeNull()

                // 2. Create new session (user logged in from new device)
                val session = repository.createSession(user.userId)
                session.userId shouldBe "returning-user"
            }

            it("should handle logout flow") {
                val user = User(userId = "user-1", authId = "auth-1")
                repository.createUser(user)
                val session = repository.createSession(user.userId)

                // User logs out
                repository.invalidateSession(session.sessionToken)

                // Session should be gone
                repository.getSession(session.sessionToken).shouldBeNull()

                // User should still exist
                repository.getUserById(user.userId).shouldNotBeNull()
            }

            it("should handle account deletion flow") {
                val user = User(userId = "user-to-delete", authId = "auth-delete")
                repository.createUser(user)
                val session = repository.createSession(user.userId)

                // Delete account
                repository.deleteUserAccount(user.userId)

                // Everything should be gone
                repository.getSession(session.sessionToken).shouldBeNull()
                repository.getUserById(user.userId).shouldBeNull()
                repository.getUserByAuthId(user.authId).shouldBeNull()
            }
        }
    }
})
