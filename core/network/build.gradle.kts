plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.dailyfocus.core.network"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
}
