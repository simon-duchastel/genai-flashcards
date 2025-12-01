package ai.solenne.flashcards.app.testing

/**
 * Annotation to mark classes as mockable.
 * Used with the Kotlin all-open plugin to make final classes open for mocking with Mokkery.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mockable
