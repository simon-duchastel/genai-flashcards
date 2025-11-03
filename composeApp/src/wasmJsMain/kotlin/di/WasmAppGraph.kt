package di

import data.api.AuthApiClient
import data.auth.OAuthHandler
import data.auth.getOAuthHandler
import data.storage.ConfigRepository
import data.storage.ConfigRepositoryJs
import data.storage.FlashcardStorage
import data.storage.FlashcardStorageJs
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
        getOAuthHandler(authApiClient)
}
