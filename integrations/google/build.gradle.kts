plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.integrations.google"
}

dependencies {
    implementation(projects.core.common)
}
