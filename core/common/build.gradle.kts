plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
