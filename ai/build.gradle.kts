plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.ai"
}

dependencies {
    api(projects.core.model)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
