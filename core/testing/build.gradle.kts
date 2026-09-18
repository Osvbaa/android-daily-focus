plugins {
    id("dailyfocus.android.library")
}

android {
    namespace = "com.example.dailyfocus.core.testing"
}

dependencies {
    implementation(projects.ai)
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.data)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.collections.immutable)
    testImplementation(libs.konsist)
    implementation(libs.kotlinx.coroutines.core)
}
