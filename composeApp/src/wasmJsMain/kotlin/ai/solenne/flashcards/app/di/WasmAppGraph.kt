package ai.solenne.flashcards.app.di

import ai.solenne.flashcards.app.data.api.AuthApiClient
import ai.solenne.flashcards.app.data.auth.OAuthHandler
import ai.solenne.flashcards.app.data.auth.WebOAuthHandler
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import ai.solenne.flashcards.app.data.storage.ConfigRepositoryJs
import ai.solenne.flashcards.app.data.storage.FlashcardStorage
import ai.solenne.flashcards.app.data.storage.FlashcardStorageJs
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * WASM-specific dependency graph.
 * Extends the base AppGraph and provides WASM-specific dependencies.
 */
@DependencyGraph(AppScope::class)
interface WasmAppGraph : AppGraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideConfigRepository(): ConfigRepository = ConfigRepositoryJs()

    @Provides
    @SingleIn(AppScope::class)
    fun provideFlashcardStorage(): FlashcardStorage = FlashcardStorageJs()

    @Provides
    fun provideOAuthHandler(authApiClient: AuthApiClient): OAuthHandler =
        WebOAuthHandler(authApiClient)
}
