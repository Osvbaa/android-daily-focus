plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.calendar"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}
