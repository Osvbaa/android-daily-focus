plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.room")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
}
