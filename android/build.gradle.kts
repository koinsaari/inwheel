plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependency.license.report) apply false
}
