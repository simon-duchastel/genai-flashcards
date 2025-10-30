package com.flashcards.server.storage

import com.flashcards.server.util.await
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import domain.model.FlashcardSet

/**
 * Firestore-backed storage for flashcard sets.
 * Provides persistent storage with the same interface as InMemoryStorage.
 *
 * Collection: flashcard_sets/{setId}
 * Documents contain: id, userId, topic, flashcards[], createdAt
 */
class FirestoreStorage(
    firestore: Firestore
) : Storage {
    private val collection = firestore.collection("flashcard_sets")

    override suspend fun save(set: FlashcardSet) {
        collection
            .document(set.id)
            .set(set)
            .await()
    }

    override suspend fun getAll(): List<FlashcardSet> {
        val querySnapshot = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(FlashcardSet::class.java)
        }
    }

    override suspend fun getById(id: String): FlashcardSet? {
        val docSnapshot = collection
            .document(id)
            .get()
            .await()

        if (!docSnapshot.exists()) {
            return null
        }

        return docSnapshot.toObject(FlashcardSet::class.java)
    }

    override suspend fun delete(id: String) {
        collection
            .document(id)
            .delete()
            .await()
    }
}
