plugins {
    id("dailyfocus.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.collections.immutable)
    api(libs.kotlinx.serialization.core)
    testImplementation(libs.junit)
}
