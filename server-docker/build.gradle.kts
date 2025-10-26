import io.ktor.plugin.features.DockerImageRegistry
import io.ktor.plugin.features.DockerPortMappingProtocol

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
}

group = "com.flashcards"
version = "1.0.0"

application {
    mainClass.set("com.flashcards.server.DockerApplicationKt")
}

dependencies {
    implementation(project(":server"))
}
ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_17)

        localImageName.set("flashcards-server")
        imageTag.set("latest")

        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                8080,
                8080,
                DockerPortMappingProtocol.TCP
            )
        ))

        externalRegistry.set(
            DockerImageRegistry.googleContainerRegistry(
                projectName = providers.environmentVariable("GCP_PROJECT_NAME"),
                appName = provider { "flashcards-server" },
                username = provider { "" }, // username and password left blank because we use gcloud auth
                password = provider { "" },
            )
        )
    }
}
