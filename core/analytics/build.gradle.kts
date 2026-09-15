plugins {
    id("dailyfocus.android.library")
}

android {
    namespace = "com.example.dailyfocus.core.analytics"
}

dependencies {
    implementation(projects.core.common)
}
