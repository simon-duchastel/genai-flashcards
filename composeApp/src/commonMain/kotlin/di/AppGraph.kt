package di

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.ui.ui
import data.api.ApiConfig
import data.api.AuthApiClient
import data.api.HttpClientProvider
import data.api.ServerFlashcardApiClient
import data.api.ServerFlashcardGenerator
import data.auth.OAuthHandler
import data.repository.AuthRepositoryImpl
import data.storage.ConfigRepository
import data.storage.FlashcardStorage
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.AppScope
import domain.generator.KoogFlashcardGenerator
import domain.repository.AuthRepository
import domain.repository.ClientFlashcardRepository
import domain.repository.LocalFlashcardRepository
import io.ktor.client.HttpClient
import presentation.auth.AuthPresenter
import presentation.auth.AuthScreen
import presentation.auth.AuthUi
import presentation.auth.AuthUiState
import presentation.create.CreatePresenter
import presentation.create.CreateScreen
import presentation.create.CreateUi
import presentation.create.CreateUiState
import presentation.home.HomePresenter
import presentation.home.HomeScreen
import presentation.home.HomeUi
import presentation.home.HomeUiState
import presentation.splash.SplashPresenter
import presentation.splash.SplashScreen
import presentation.splash.SplashUi
import presentation.splash.SplashUiState
import presentation.study.StudyPresenter
import presentation.study.StudyScreen
import presentation.study.StudyUi
import presentation.study.StudyUiState

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
                CreateScreen -> CreatePresenter(navigator, authRepository, clientRepository, localRepository, serverGenerator, koogGenerator)
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
                CreateScreen -> ui<CreateUiState> { state, modifier ->
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
