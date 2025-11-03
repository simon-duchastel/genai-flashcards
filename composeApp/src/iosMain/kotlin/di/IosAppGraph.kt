package di

import data.api.AuthApiClient
import data.auth.OAuthHandler
import data.auth.getOAuthHandler
import data.storage.ConfigRepository
import data.storage.ConfigRepositoryIos
import data.storage.FlashcardStorage
import data.storage.FlashcardStorageIos
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * iOS-specific dependency graph.
 * Extends the base AppGraph and provides iOS-specific dependencies.
 */
@DependencyGraph(AppScope::class)
interface IosAppGraph : AppGraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideConfigRepository(): ConfigRepository = ConfigRepositoryIos()

    @Provides
    @SingleIn(AppScope::class)
    fun provideFlashcardStorage(): FlashcardStorage = FlashcardStorageIos()

    @Provides
    fun provideOAuthHandler(authApiClient: AuthApiClient): OAuthHandler =
        getOAuthHandler(authApiClient)
}
