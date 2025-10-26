package com.flashcards.server.docker

import com.flashcards.server.main as multiplatformMain

/**
 * Shim to allow Docker to see the multiplatform main function
 */
fun main() {
    multiplatformMain()
}