plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.library.compose")
}

android {
    namespace = "com.example.dailyfocus.core.ui"
}

dependencies {
    api(projects.core.model)
}
