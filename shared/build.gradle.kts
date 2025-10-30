import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm() // For server

    androidTarget() // For Android app

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

            // Coroutines for suspend functions
            implementation(libs.kotlinx.coroutines.core)

            // Kotlinx datetime (required by koog-agents)
            implementation(libs.kotlinx.datetime)

            // Koog AI agents (for flashcard generation)
            api(libs.koog.agents)
            api(libs.koog.ktor)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "ai.solenne.flashcards.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
