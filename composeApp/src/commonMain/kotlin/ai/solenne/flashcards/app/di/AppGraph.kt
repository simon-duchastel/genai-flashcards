package ai.solenne.flashcards.app.di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import ai.solenne.flashcards.app.data.api.ApiConfig
import ai.solenne.flashcards.app.data.api.AuthApiClient
import ai.solenne.flashcards.app.data.api.HttpClientProvider
import ai.solenne.flashcards.app.data.api.ServerFlashcardApiClient
import ai.solenne.flashcards.app.data.api.ServerFlashcardGenerator
import ai.solenne.flashcards.app.data.auth.OAuthHandler
import ai.solenne.flashcards.app.data.repository.AuthRepositoryImpl
import ai.solenne.flashcards.app.data.storage.ConfigRepository
import ai.solenne.flashcards.app.data.storage.FlashcardStorage
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.AppScope
import ai.solenne.flashcards.shared.domain.generator.KoogFlashcardGenerator
import ai.solenne.flashcards.app.domain.repository.AuthRepository
import ai.solenne.flashcards.app.domain.repository.ClientFlashcardRepository
import ai.solenne.flashcards.app.domain.repository.LocalFlashcardRepository
import io.ktor.client.HttpClient
import ai.solenne.flashcards.app.presentation.auth.AuthPresenter
import ai.solenne.flashcards.app.presentation.auth.AuthScreen
import ai.solenne.flashcards.app.presentation.auth.AuthUi
import ai.solenne.flashcards.app.presentation.auth.AuthUiState
import ai.solenne.flashcards.app.presentation.create.CreatePresenter
import ai.solenne.flashcards.app.presentation.create.CreateScreen
import ai.solenne.flashcards.app.presentation.create.CreateUi
import ai.solenne.flashcards.app.presentation.create.CreateUiState
import ai.solenne.flashcards.app.presentation.home.HomePresenter
import ai.solenne.flashcards.app.presentation.home.HomeScreen
import ai.solenne.flashcards.app.presentation.home.HomeUi
import ai.solenne.flashcards.app.presentation.home.HomeUiState
import ai.solenne.flashcards.app.presentation.splash.SplashPresenter
import ai.solenne.flashcards.app.presentation.splash.SplashScreen
import ai.solenne.flashcards.app.presentation.splash.SplashUi
import ai.solenne.flashcards.app.presentation.splash.SplashUiState
import ai.solenne.flashcards.app.presentation.study.StudyPresenter
import ai.solenne.flashcards.app.presentation.study.StudyScreen
import ai.solenne.flashcards.app.presentation.study.StudyUi
import ai.solenne.flashcards.app.presentation.study.StudyUiState

/**
 * Base dependency graph for the application.
 * Platform-specific graphs should extend this interface and add @DependencyGraph annotation.
 * This allows each platform to provide its own platform-specific dependencies.
 */
interface AppGraph {
    val circuit: Circuit
    val configRepository: ConfigRepository
    val authApiClient: AuthApiClient

    @Provides
    fun provideHttpClient(): HttpClient = HttpClientProvider.client

    @Provides
    fun provideBaseUrl(): String = ApiConfig.BASE_URL

    @Provides
    fun provideAuthRepository(configRepository: ConfigRepository): AuthRepository =
        AuthRepositoryImpl(configRepository)

    @Provides
    fun provideServerFlashcardApiClient(
        httpClient: HttpClient,
        baseUrl: String
    ): ServerFlashcardApiClient = ServerFlashcardApiClient(httpClient, baseUrl)

    @Provides
    fun provideAuthApiClient(
        httpClient: HttpClient,
        baseUrl: String
    ): AuthApiClient = AuthApiClient(isTest = false, httpClient, baseUrl)

    @Provides
    fun provideServerFlashcardGenerator(
        httpClient: HttpClient,
        baseUrl: String,
        configRepository: ConfigRepository
    ): ServerFlashcardGenerator = ServerFlashcardGenerator(httpClient, baseUrl, configRepository)

    @Provides
    fun provideKoogFlashcardGenerator(
        configRepository: ConfigRepository
    ): KoogFlashcardGenerator = KoogFlashcardGenerator(
        getGeminiApiKey = configRepository::getGeminiApiKey
    )

    @Provides
    fun provideClientRepository(
        authRepository: AuthRepository,
        serverClient: ServerFlashcardApiClient
    ): ClientFlashcardRepository = ClientFlashcardRepository(authRepository, serverClient)

    @Provides
    fun provideLocalRepository(
        storage: FlashcardStorage
    ): LocalFlashcardRepository = LocalFlashcardRepository(storage)

    @Provides
    @SingleIn(AppScope::class)
    fun provideCircuit(
        configRepository: ConfigRepository,
        oauthHandler: OAuthHandler,
        authApiClient: AuthApiClient,
        authRepository: AuthRepository,
        clientRepository: ClientFlashcardRepository,
        localRepository: LocalFlashcardRepository,
        serverGenerator: ServerFlashcardGenerator,
        koogGenerator: KoogFlashcardGenerator
    ): Circuit = Circuit.Builder()
        .addPresenterFactory { screen, navigator, _ ->
            when (screen) {
                is SplashScreen -> SplashPresenter(navigator, configRepository)
                is AuthScreen -> AuthPresenter(navigator, configRepository, oauthHandler, authApiClient)
                is HomeScreen -> HomePresenter(navigator, authRepository, clientRepository, localRepository)
                is CreateScreen -> CreatePresenter(screen, navigator, authRepository, clientRepository, localRepository, serverGenerator, koogGenerator)
                is StudyScreen -> StudyPresenter(screen, navigator, authRepository, clientRepository, localRepository)
                else -> null
            }
        }
        .addUiFactory { screen, _ ->
            when (screen) {
                is SplashScreen -> ui<SplashUiState> { state, modifier ->
                    SplashUi(state, modifier)
                }
                is AuthScreen -> ui<AuthUiState> { state, modifier ->
                    AuthUi(state, modifier)
                }
                is HomeScreen -> ui<HomeUiState> { state, modifier ->
                    HomeUi(state, modifier)
                }
                is CreateScreen -> ui<CreateUiState> { state, modifier ->
                    CreateUi(state, modifier)
                }
                is StudyScreen -> ui<StudyUiState> { state, modifier ->
                    StudyUi(state, modifier)
                }
                else -> null
            }
        }
        .build()
}
