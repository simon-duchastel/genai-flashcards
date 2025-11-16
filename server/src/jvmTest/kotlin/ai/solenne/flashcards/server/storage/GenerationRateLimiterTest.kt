package ai.solenne.flashcards.server.storage

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.longs.shouldBeGreaterThan

class GenerationRateLimiterTest : DescribeSpec({
    lateinit var rateLimiter: GenerationRateLimiter

    beforeEach {
        rateLimiter = GenerationRateLimiter()
    }

    describe("GenerationRateLimiter") {
        describe("getUserLimit") {
            it("should return default limit of 20 for new user") {
                val limit = rateLimiter.getUserLimit("new-user")
                limit shouldBe 20
            }

            it("should return custom limit after it's set") {
                rateLimiter.setUserLimit("premium-user", 100)
                val limit = rateLimiter.getUserLimit("premium-user")
                limit shouldBe 100
            }

            it("should return default for user without custom limit") {
                rateLimiter.setUserLimit("user-1", 50)
                // user-2 should still get default
                rateLimiter.getUserLimit("user-2") shouldBe 20
            }
        }

        describe("setUserLimit") {
            it("should set custom limit for user") {
                rateLimiter.setUserLimit("user-123", 50)
                rateLimiter.getUserLimit("user-123") shouldBe 50
            }

            it("should allow setting limit to zero") {
                rateLimiter.setUserLimit("blocked-user", 0)
                rateLimiter.getUserLimit("blocked-user") shouldBe 0
            }

            it("should allow setting very high limits") {
                rateLimiter.setUserLimit("unlimited-user", 10000)
                rateLimiter.getUserLimit("unlimited-user") shouldBe 10000
            }

            it("should override previous limit") {
                rateLimiter.setUserLimit("user-123", 50)
                rateLimiter.setUserLimit("user-123", 100)
                rateLimiter.getUserLimit("user-123") shouldBe 100
            }
        }

        describe("checkRateLimit") {
            it("should return Ok for user with no attempts") {
                val result = rateLimiter.checkRateLimit("new-user")
                result.shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should return Ok when under limit") {
                val userId = "test-user"
                // Record 5 attempts (default limit is 20)
                repeat(5) {
                    rateLimiter.recordAttempt(userId)
                }

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should return RateLimitExceeded when at limit") {
                val userId = "limited-user"
                // Record 20 attempts (default limit)
                repeat(20) {
                    rateLimiter.recordAttempt(userId)
                }

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }

            it("should return RateLimitExceeded with correct info") {
                val userId = "exceeded-user"
                repeat(20) {
                    rateLimiter.recordAttempt(userId)
                }

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()

                val exceeded = result as RateLimitResult.RateLimitExceeded
                exceeded.numberOfGenerations shouldBe 20
                exceeded.tryAgainAt shouldBeGreaterThan System.currentTimeMillis()
            }

            it("should respect custom user limits") {
                val userId = "custom-limit-user"
                rateLimiter.setUserLimit(userId, 5)

                // Record 5 attempts
                repeat(5) {
                    rateLimiter.recordAttempt(userId)
                }

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }

            it("should allow more attempts for higher limits") {
                val userId = "high-limit-user"
                rateLimiter.setUserLimit(userId, 50)

                // Record 30 attempts
                repeat(30) {
                    rateLimiter.recordAttempt(userId)
                }

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should track attempts per user independently") {
                val user1 = "user-1"
                val user2 = "user-2"

                // user1 hits limit
                repeat(20) {
                    rateLimiter.recordAttempt(user1)
                }

                // user2 is still under limit
                repeat(5) {
                    rateLimiter.recordAttempt(user2)
                }

                rateLimiter.checkRateLimit(user1).shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
                rateLimiter.checkRateLimit(user2).shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should block user with zero limit") {
                val userId = "blocked-user"
                rateLimiter.setUserLimit(userId, 0)

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.Ok>() // No attempts yet, but 0 limit means they can't generate

                // Actually, with 0 limit and 0 attempts, it should pass
                // But one attempt should exceed
                rateLimiter.recordAttempt(userId)

                val afterAttempt = rateLimiter.checkRateLimit(userId)
                afterAttempt.shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }
        }

        describe("recordAttempt") {
            it("should record a generation attempt") {
                val userId = "test-user"
                rateLimiter.recordAttempt(userId)

                // After one attempt, should still be OK (default limit is 20)
                rateLimiter.checkRateLimit(userId).shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should accumulate attempts") {
                val userId = "cumulative-user"
                rateLimiter.setUserLimit(userId, 3)

                rateLimiter.recordAttempt(userId)
                rateLimiter.checkRateLimit(userId).shouldBeInstanceOf<RateLimitResult.Ok>()

                rateLimiter.recordAttempt(userId)
                rateLimiter.checkRateLimit(userId).shouldBeInstanceOf<RateLimitResult.Ok>()

                rateLimiter.recordAttempt(userId)
                rateLimiter.checkRateLimit(userId).shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }

            it("should track attempts separately per user") {
                rateLimiter.recordAttempt("user-a")
                rateLimiter.recordAttempt("user-b")
                rateLimiter.recordAttempt("user-b")

                // user-a has 1 attempt
                rateLimiter.setUserLimit("user-a", 2)
                rateLimiter.checkRateLimit("user-a").shouldBeInstanceOf<RateLimitResult.Ok>()

                // user-b has 2 attempts
                rateLimiter.setUserLimit("user-b", 2)
                rateLimiter.checkRateLimit("user-b").shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }
        }

        describe("rate limit scenarios") {
            it("should handle typical free user workflow") {
                val freeUser = "free-user"
                // Free users get default 20 generations/24h

                // User generates throughout the day
                repeat(10) {
                    val check = rateLimiter.checkRateLimit(freeUser)
                    check.shouldBeInstanceOf<RateLimitResult.Ok>()
                    rateLimiter.recordAttempt(freeUser)
                }

                // Still have 10 left
                rateLimiter.checkRateLimit(freeUser).shouldBeInstanceOf<RateLimitResult.Ok>()

                // Use remaining 10
                repeat(10) {
                    rateLimiter.recordAttempt(freeUser)
                }

                // Now exceeded
                rateLimiter.checkRateLimit(freeUser).shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()
            }

            it("should handle premium user with higher limit") {
                val premiumUser = "premium-user"
                rateLimiter.setUserLimit(premiumUser, 100)

                // Premium user can generate more
                repeat(50) {
                    rateLimiter.recordAttempt(premiumUser)
                }

                // Still under limit
                rateLimiter.checkRateLimit(premiumUser).shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should provide accurate retry time information") {
                val userId = "timed-user"
                rateLimiter.setUserLimit(userId, 1)
                rateLimiter.recordAttempt(userId)

                val result = rateLimiter.checkRateLimit(userId)
                result.shouldBeInstanceOf<RateLimitResult.RateLimitExceeded>()

                val exceeded = result as RateLimitResult.RateLimitExceeded
                // tryAgainAt should be approximately 24 hours from now
                val expectedTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                // Allow 1 minute tolerance
                val tolerance = 60 * 1000
                (exceeded.tryAgainAt - expectedTime).let { diff ->
                    kotlin.math.abs(diff) shouldBe (diff < tolerance)
                }
            }
        }

        describe("edge cases") {
            it("should handle rapid successive checks") {
                val userId = "rapid-checker"
                repeat(1000) {
                    rateLimiter.checkRateLimit(userId)
                }
                // Should not throw or cause issues
            }

            it("should handle empty user ID") {
                val result = rateLimiter.checkRateLimit("")
                result.shouldBeInstanceOf<RateLimitResult.Ok>()
            }

            it("should handle very long user ID") {
                val longUserId = "x".repeat(10000)
                val result = rateLimiter.checkRateLimit(longUserId)
                result.shouldBeInstanceOf<RateLimitResult.Ok>()
            }
        }
    }
})
