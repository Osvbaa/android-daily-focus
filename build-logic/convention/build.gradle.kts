plugins {
    `kotlin-dsl`
}

group = "com.example.dailyfocus.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}
