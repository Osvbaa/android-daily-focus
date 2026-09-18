plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.dashboard"
}
dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.core.ktx)
}
