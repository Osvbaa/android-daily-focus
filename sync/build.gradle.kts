plugins {
    id("dailyfocus.android.library")
    id("dailyfocus.android.hilt")
}

android {
    namespace = "com.example.dailyfocus.sync"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(libs.androidx.work.runtime.ktx)
}
