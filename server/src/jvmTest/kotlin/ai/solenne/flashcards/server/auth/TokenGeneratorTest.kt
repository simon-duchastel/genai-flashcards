package ai.solenne.flashcards.server.auth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldBeUnique

class TokenGeneratorTest : DescribeSpec({
    describe("TokenGenerator") {
        describe("generateSessionToken") {
            it("should generate a token of 64 characters (256 bits in hex)") {
                val token = TokenGenerator.generateSessionToken()
                token.shouldHaveLength(64)
            }

            it("should generate only hexadecimal characters") {
                val token = TokenGenerator.generateSessionToken()
                token shouldMatch Regex("^[0-9a-f]+$")
            }

            it("should generate unique tokens on each call") {
                val token1 = TokenGenerator.generateSessionToken()
                val token2 = TokenGenerator.generateSessionToken()

                token1 shouldNotBe token2
            }

            it("should generate many unique tokens") {
                val tokens = (1..100).map { TokenGenerator.generateSessionToken() }
                tokens.shouldBeUnique()
            }

            it("should not generate tokens with uppercase letters") {
                val token = TokenGenerator.generateSessionToken()
                // Hex format uses lowercase
                token shouldMatch Regex("^[0-9a-f]{64}$")
            }

            it("should have sufficient entropy for security") {
                // 32 bytes = 256 bits of entropy
                // Token is hex encoded, so 64 characters
                val token = TokenGenerator.generateSessionToken()
                token.shouldHaveLength(64)

                // Verify it's not a trivial pattern
                val uniqueChars = token.toSet()
                // With good randomness, we should have many unique characters
                // Statistically, with 64 hex chars, we should have close to 16 unique chars
                // We'll be lenient and just check it's not trivial
                uniqueChars.size shouldNotBe 1 // Not all same character
            }

            it("should handle rapid successive calls") {
                // Generate tokens in quick succession
                val tokens = mutableListOf<String>()
                repeat(1000) {
                    tokens.add(TokenGenerator.generateSessionToken())
                }

                tokens.shouldHaveSize(1000)
                tokens.shouldBeUnique()
            }
        }

        describe("token format validation") {
            it("should be usable as a session identifier") {
                val token = TokenGenerator.generateSessionToken()

                // Should be suitable for HTTP headers
                token.none { it.isWhitespace() } shouldBe true
                token.none { it == '\n' || it == '\r' } shouldBe true
            }

            it("should be case-consistent (lowercase)") {
                repeat(10) {
                    val token = TokenGenerator.generateSessionToken()
                    token shouldBe token.lowercase()
                }
            }
        }

        describe("statistical properties") {
            it("should have reasonable character distribution") {
                // Generate a sample of tokens and check distribution
                val allChars = (1..100)
                    .map { TokenGenerator.generateSessionToken() }
                    .joinToString("")

                // Count occurrences of each hex digit
                val charCounts = allChars.groupingBy { it }.eachCount()

                // Should have all hex digits represented
                val hexDigits = "0123456789abcdef"
                hexDigits.forEach { digit ->
                    charCounts.containsKey(digit) shouldBe true
                }
            }
        }
    }
})
