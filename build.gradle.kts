plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// AGP's built-in Kotlin defaults to an older compiler. Pin the same version as the Compose compiler.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}
