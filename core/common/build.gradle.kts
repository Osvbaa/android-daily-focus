plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.dailyfocus.core.common"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
