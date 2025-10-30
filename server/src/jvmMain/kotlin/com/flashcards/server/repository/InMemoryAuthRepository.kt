package com.flashcards.server.repository

import com.flashcards.server.auth.TokenGenerator
import domain.model.Session
import domain.model.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of AuthRepository.
 * Thread-safe using Mutex for concurrent access.
 *
 * Note: Sessions and users are lost on server restart.
 * This is acceptable for development/MVP phase.
 */
class InMemoryAuthRepository : AuthRepository {
    private val sessions = mutableMapOf<String, Session>()
    private val users = mutableMapOf<String, User>()
    private val authIdToUserId = mutableMapOf<String, String>()
    private val mutex = Mutex()

    override suspend fun createSession(userId: String): Session {
        val token = TokenGenerator.generateSessionToken()
        val session = Session(
            sessionToken = token,
            userId = userId
        )

        mutex.withLock {
            sessions[token] = session
        }

        return session
    }

    override suspend fun getSession(token: String): Session? {
        return mutex.withLock {
            val session = sessions[token]
            // Return null if session expired
            if (session != null && session.isExpired()) {
                sessions.remove(token)
                null
            } else {
                session
            }
        }
    }

    override suspend fun invalidateSession(token: String) {
        mutex.withLock {
            sessions.remove(token)
        }
    }

    override suspend fun getUserByAuthId(authId: String): User? {
        return mutex.withLock {
            val userId = authIdToUserId[authId]
            userId?.let { users[it] }
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return mutex.withLock {
            users[userId]
        }
    }

    override suspend fun createUser(user: User): User {
        mutex.withLock {
            users[user.userId] = user
            authIdToUserId[user.authId] = user.userId
        }
        return user
    }

    override suspend fun updateSessionAccess(token: String) {
        mutex.withLock {
            sessions[token]?.let { session ->
                sessions[token] = session.updateLastAccessed()
            }
        }
    }

    override suspend fun deleteUserAccount(userId: String) {
        mutex.withLock {
            // Get user to retrieve authId
            val user = users[userId] ?: return@withLock

            // 1. Delete all sessions for this user
            sessions.entries.removeIf { it.value.userId == userId }

            // 2. Delete authId -> userId mapping
            authIdToUserId.remove(user.authId)

            // 3. Delete user
            users.remove(userId)
        }
    }
}
