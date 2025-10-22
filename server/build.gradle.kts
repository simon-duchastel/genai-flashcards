plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm {
        // Configure JVM target as an application
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                // Shared module
                implementation(project(":shared"))

                // Ktor server
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.server.call.logging)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.ktor.server.auth)
                implementation(libs.ktor.client.apache)
                implementation(libs.ktor.client.content.negotiation)

                // Kotlinx
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // Logging
                implementation(libs.logback.classic)
            }
        }
    }
}

// Create a run task for the server
tasks.register<JavaExec>("run") {
    group = "application"
    mainClass.set("com.flashcards.server.ApplicationKt")

    val mainCompilation = kotlin.jvm().compilations.getByName("main")
    classpath = files(
        mainCompilation.output.allOutputs,
        mainCompilation.runtimeDependencyFiles
    )
}
