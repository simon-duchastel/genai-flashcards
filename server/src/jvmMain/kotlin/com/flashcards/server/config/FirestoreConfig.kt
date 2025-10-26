package com.flashcards.server.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream

/**
 * Configuration and initialization for Firestore.
 *
 * Supports two authentication modes:
 * 1. Service Account Key (via GOOGLE_APPLICATION_CREDENTIALS env var)
 * 2. Application Default Credentials (for Cloud Run/GCE - automatic)
 */
object FirestoreConfig {
    private var firestore: Firestore? = null

    /**
     * Initialize Firestore with credentials.
     *
     * Authentication priority:
     * 1. GOOGLE_APPLICATION_CREDENTIALS - path to service account JSON (for local development)
     * 2. Application Default Credentials (ADC) - automatic in GCP (Cloud Run, GCE, GKE)
     *
     * @throws IllegalStateException if credentials cannot be found
     */
    fun initialize(): Firestore {
        if (firestore != null) {
            return firestore!!
        }

        val projectId = System.getenv("FIRESTORE_PROJECT_ID")
            ?: error("FIRESTORE_PROJECT_ID environment variable not set")

        val credentials = try {
            val serviceAccountPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
            if (!serviceAccountPath.isNullOrBlank()) {
                // Local development: use service account key file
                println("Using service account credentials from: $serviceAccountPath")
                GoogleCredentials.fromStream(FileInputStream(serviceAccountPath))
            } else {
                // Cloud Run/GCP: use Application Default Credentials
                println("Using Application Default Credentials (ADC)")
                GoogleCredentials.getApplicationDefault()
            }
        } catch (e: Exception) {
            error("Failed to initialize Firestore credentials: ${e.message}\n" +
                  "Make sure FIRESTORE_PROJECT_ID is set and you're either:\n" +
                  "1. Running on GCP (Cloud Run, GCE, GKE) with proper permissions, or\n" +
                  "2. Have GOOGLE_APPLICATION_CREDENTIALS pointing to a valid service account key")
        }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }

        firestore = FirestoreClient.getFirestore()
        println("Firestore initialized successfully for project: $projectId")
        return firestore!!
    }

    /**
     * Get the initialized Firestore instance.
     *
     * @throws IllegalStateException if Firestore has not been initialized
     */
    fun getFirestore(): Firestore {
        return firestore ?: error("Firestore not initialized. Call initialize() first.")
    }
}
