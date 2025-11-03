package ai.solenne.flashcards.app.di

import ai.solenne.flashcards.app.data.api.AuthApiClient
import ai.solenne.flashcards.app.data.auth.OAuthHandler
import ai.solenne.flashcards.app.data.auth.SFOAuthHandler
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import ai.solenne.flashcards.app.data.storage.ConfigRepositoryIos
import ai.solenne.flashcards.app.data.storage.FlashcardStorage
import ai.solenne.flashcards.app.data.storage.FlashcardStorageIos
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
       SFOAuthHandler(authApiClient)
}
