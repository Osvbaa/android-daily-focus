plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.compose")
}

android {
    namespace = "com.example.dailyfocus.core.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.material.icons.extended)
}
