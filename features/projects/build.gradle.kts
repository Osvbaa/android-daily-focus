plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.projects"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(libs.kotlinx.collections.immutable)
}
