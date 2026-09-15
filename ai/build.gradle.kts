plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.ai"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.openai.client)
}
