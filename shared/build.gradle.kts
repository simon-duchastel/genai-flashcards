import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm() // For server

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Kotlinx serialization for DTOs and models
            implementation(libs.kotlinx.serialization.json)

            // Coroutines for suspend functions
            implementation(libs.kotlinx.coroutines.core)

            // Koog AI agents (for flashcard generation)
            api(libs.koog.agents)
            api(libs.koog.ktor)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
