package ai.solenne.flashcards.app.di

import android.content.Context
import ai.solenne.flashcards.app.data.api.AuthApiClient
import ai.solenne.flashcards.app.data.auth.ChromeCustomTabsOAuthHandler
import ai.solenne.flashcards.app.data.auth.OAuthHandler
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import ai.solenne.flashcards.app.data.storage.ConfigRepositoryAndroid
import ai.solenne.flashcards.app.data.storage.FlashcardStorage
import ai.solenne.flashcards.app.data.storage.FlashcardStorageAndroid
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Android-specific dependency graph.
 * Extends the base AppGraph and provides Android-specific dependencies like Context.
 */
@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideConfigRepository(context: Context): ConfigRepository =
        ConfigRepositoryAndroid(context)

    @Provides
    @SingleIn(AppScope::class)
    fun provideFlashcardStorage(context: Context): FlashcardStorage =
        FlashcardStorageAndroid(context)

    @Provides
    fun provideOAuthHandler(context: Context, authApiClient: AuthApiClient): OAuthHandler =
        ChromeCustomTabsOAuthHandler(context, authApiClient)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides appContext: Context): AndroidAppGraph
    }
}
