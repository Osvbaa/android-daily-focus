plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.habits"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(libs.kotlinx.collections.immutable)
}
