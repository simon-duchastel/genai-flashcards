package com.flashcards.server.auth

import java.security.SecureRandom

/**
 * Utility for generating cryptographically secure session tokens.
 */
object TokenGenerator {
    private val secureRandom = SecureRandom()
    private const val TOKEN_BYTE_LENGTH = 32 // 256 bits

    /**
     * Generate a cryptographically secure random session token.
     *
     * @return 64-character hex string (256 bits of entropy)
     */
    fun generateSessionToken(): String {
        val bytes = ByteArray(TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(bytes)
        return bytes.toHex()
    }

    /**
     * Convert byte array to hexadecimal string.
     */
    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
