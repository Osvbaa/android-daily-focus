plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.library.compose")
}

android {
    namespace = "com.example.dailyfocus.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.material.icons.extended)
}
