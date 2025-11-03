package ai.solenne.flashcards.app.data.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "config")

class ConfigRepositoryAndroid(private val context: Context) : ConfigRepository {
    private val apiKeyKey = stringPreferencesKey("gemini_api_key")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val sessionTokenKey = stringPreferencesKey("session_token")

    override suspend fun getGeminiApiKey(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[apiKeyKey]
        }.first()
    }

    override suspend fun setGeminiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[apiKeyKey] = apiKey
        }
    }

    override suspend fun isDarkMode(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[darkModeKey] ?: false
        }.first()
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[darkModeKey] = isDark
        }
    }

    override suspend fun getSessionToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[sessionTokenKey]
        }.first()
    }

    override suspend fun setSessionToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[sessionTokenKey] = token
        }
    }

    override suspend fun clearSessionToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(sessionTokenKey)
        }
    }
}

private lateinit var configRepositoryInstance: ConfigRepository

fun initConfigRepository(context: Context) {
    configRepositoryInstance = ConfigRepositoryAndroid(context)
}

actual fun getConfigRepository(): ConfigRepository = configRepositoryInstance
