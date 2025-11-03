package ai.solenne.flashcards.server.storage

import ai.solenne.flashcards.shared.domain.storage.Storage as SharedStorage

// Re-export shared Storage interface for backward compatibility
// All server code should eventually import from ai.solenne.flashcards.shared.domain.storage.Storage
typealias Storage = SharedStorage
