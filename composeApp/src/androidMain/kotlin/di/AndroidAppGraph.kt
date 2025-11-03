package di

import android.content.Context
import data.api.AuthApiClient
import data.auth.OAuthHandler
import data.auth.getOAuthHandler
import data.storage.ConfigRepository
import data.storage.ConfigRepositoryAndroid
import data.storage.FlashcardStorage
import data.storage.FlashcardStorageAndroid
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
        getOAuthHandler(context, authApiClient)
}
