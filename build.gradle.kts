buildscript {
    dependencies {
        classpath("commons-codec:commons-codec:1.16.1")
    }
    configurations.all {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.26.0")
            force("commons-codec:commons-codec:1.16.1")
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinParcelize) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
}
