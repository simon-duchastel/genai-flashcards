package data.api

import kotlinx.browser.window

/**
 * API configuration for the web platform.
 */
object ApiConfig {
    /**
     * Base URL for API calls.
     * In development, uses the current origin (e.g., http://localhost:8080).
     * In production, could be configured via environment variables.
     */
    val BASE_URL: String
        get() {
            val origin = window.location.origin
            // Remove trailing slash if present
            return origin.trimEnd('/')
        }
}
