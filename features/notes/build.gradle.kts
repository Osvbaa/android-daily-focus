plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.notes"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.collections.immutable)
    implementation(projects.ai)
    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}
