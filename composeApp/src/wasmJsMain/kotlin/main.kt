import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import data.api.AuthApiClient
import data.storage.ConfigRepository
import di.WasmAppGraph
import di.createGraphFactory
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * External interface for accessing URL search params.
 */
external interface URLSearchParams : JsAny {
    fun get(name: String): String?
}

/**
 * Create URLSearchParams from window.location.search
 */
@JsFun("() => new URLSearchParams(window.location.search)")
private external fun getURLSearchParams(): URLSearchParams

/**
 * Get query parameter value from URL.
 */
private fun getQueryParam(name: String): String? {
    return getURLSearchParams().get(name)
}

@OptIn(
    ExperimentalComposeUiApi::class,
    DelicateCoroutinesApi::class,
    ExperimentalWasmJsInterop::class,
)
fun main() {
    val appGraph = createGraphFactory<WasmAppGraph.Factory>().create()

    // check if we've been redirected to from auth sign-in
    GlobalScope.launch {
        if (getQueryParam("auth-redirect") == "true") {
            val token = getQueryParam("token")
            if (token != null) {
                // Save session token to localStorage
                appGraph.configRepository.setSessionToken(token)
                window.history.replaceState(null, "", "/")

                configureUserSession(token, appGraph.configRepository, appGraph.authApiClient)
            }
        } else {
            val sessionToken = appGraph.configRepository.getSessionToken()
            if (sessionToken != null) {
                configureUserSession(sessionToken, appGraph.configRepository, appGraph.authApiClient)
            }
        }
    }

    ComposeViewport(
        content = {
            SelectionContainer {
                App(
                    configRepository = appGraph.configRepository,
                    circuit = appGraph.circuit,
                )
            }
        }
    )
}

private suspend fun configureUserSession(
    sessionToken: String,
    configRepository: ConfigRepository,
    authApiClient: AuthApiClient,
) {
    try {
        // /me response currently unused, may be used in a future version
//        val meResponse = authApiClient.getMe(sessionToken)
    } catch (_: Exception) {
        // If /me fails, clear invalid session
        configRepository.clearSessionToken()
    }
}