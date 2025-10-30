package com.flashcards.server.repository

import com.flashcards.server.auth.TokenGenerator
import com.flashcards.server.util.await
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.cloud.firestore.Firestore
import domain.model.Session
import domain.model.User
import java.util.concurrent.TimeUnit

/**
 * Firestore implementation of AuthRepository.
 * Uses Firestore for persistent storage with in-memory caching for performance.
 *
 * Collections:
 * - users/{userId}: User documents
 * - users_by_auth_id/{authId}: Mapping documents (authId -> userId)
 * - sessions/{sessionToken}: Session documents
 *
 * Caching strategy:
 * - Sessions cached for 5 minutes (reduces Firestore reads on every request)
 */
class FirestoreAuthRepository(
    private val firestore: Firestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")
    private val authIdMappingCollection = firestore.collection("users_by_auth_id")
    private val sessionsCollection = firestore.collection("sessions")

    // Session cache: 5-minute expiry, max 10,000 sessions
    private val sessionCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build<String, Session>()

    override suspend fun createSession(userId: String): Session {
        val token = TokenGenerator.generateSessionToken()
        val session = Session(
            sessionToken = token,
            userId = userId
        )

        // Save to Firestore
        sessionsCollection
            .document(token)
            .set(session)
            .await()

        // Cache the session
        sessionCache.put(token, session)

        return session
    }

    override suspend fun getSession(token: String): Session? {
        // Check cache first
        sessionCache.getIfPresent(token)?.let { cachedSession ->
            // Validate expiration
            if (cachedSession.isExpired()) {
                // Remove from cache and Firestore
                sessionCache.invalidate(token)
                sessionsCollection.document(token).delete().await()
                return null
            }
            return cachedSession
        }

        // Cache miss - fetch from Firestore
        val docSnapshot = sessionsCollection
            .document(token)
            .get()
            .await()

        if (!docSnapshot.exists()) {
            return null
        }

        val session = docSnapshot.toObject(Session::class.java)
            ?: return null

        // Check if expired
        if (session.isExpired()) {
            sessionsCollection.document(token).delete().await()
            return null
        }

        // Cache and return
        sessionCache.put(token, session)
        return session
    }

    override suspend fun invalidateSession(token: String) {
        // Remove from cache
        sessionCache.invalidate(token)

        // Remove from Firestore
        sessionsCollection
            .document(token)
            .delete()
            .await()
    }

    override suspend fun getUserByAuthId(authId: String): User? {
        // Look up userId from authId mapping
        val mappingDoc = authIdMappingCollection
            .document(authId)
            .get()
            .await()

        if (!mappingDoc.exists()) {
            return null
        }

        val userId = mappingDoc.get("userId") as? String ?: return null

        // Fetch user by userId
        return getUserById(userId)
    }

    override suspend fun getUserById(userId: String): User? {
        val docSnapshot = usersCollection
            .document(userId)
            .get()
            .await()

        if (!docSnapshot.exists()) {
            return null
        }

        return docSnapshot.toObject(User::class.java)
    }

    override suspend fun createUser(user: User): User {
        // Store user document
        usersCollection
            .document(user.userId)
            .set(user)
            .await()

        // Store authId -> userId mapping
        authIdMappingCollection
            .document(user.authId)
            .set(mapOf("userId" to user.userId))
            .await()

        return user
    }

    override suspend fun updateSessionAccess(token: String) {
        // Get session (from cache or Firestore)
        val session = getSession(token) ?: return

        // Update with new timestamp
        val updatedSession = session.updateLastAccessed()

        // Update in Firestore
        sessionsCollection
            .document(token)
            .set(updatedSession)
            .await()

        // Update cache
        sessionCache.put(token, updatedSession)
    }

    override suspend fun deleteUserAccount(userId: String) {
        // Get user to retrieve authId
        val user = getUserById(userId) ?: return

        // 1. Delete all sessions for this user
        val sessionsQuery = sessionsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()

        sessionsQuery.documents.forEach { doc ->
            // Remove from cache
            sessionCache.invalidate(doc.id)
            // Delete from Firestore
            doc.reference.delete().await()
        }

        // 2. Delete authId -> userId mapping
        authIdMappingCollection
            .document(user.authId)
            .delete()
            .await()

        // 3. Delete user document
        usersCollection
            .document(userId)
            .delete()
            .await()
    }
}
