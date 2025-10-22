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

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Kotlinx serialization for DTOs and models
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

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
