plugins {
    id("dailyfocus.android.feature")
}

android {
    namespace = "com.example.dailyfocus.features.today"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.collections.immutable)
    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}
