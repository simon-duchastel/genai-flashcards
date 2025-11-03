package ai.solenne.flashcards.server.docker

import ai.solenne.flashcards.server.main as multiplatformMain

/**
 * Shim to allow Docker to see the multiplatform main function
 */
fun main() {
    multiplatformMain()
}