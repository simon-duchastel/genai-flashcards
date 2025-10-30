package com.flashcards.server.storage

// Re-export shared Storage interface for backward compatibility
// All server code should eventually import from domain.storage.Storage
typealias Storage = domain.storage.Storage
