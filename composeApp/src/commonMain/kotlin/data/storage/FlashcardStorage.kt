package data.storage

import domain.storage.Storage

/**
 * Platform-specific storage for flashcard sets.
 * Uses DataStore on Android, localStorage on JS/Browser platform.
 */
typealias FlashcardStorage = Storage

expect fun getFlashcardStorage(): FlashcardStorage