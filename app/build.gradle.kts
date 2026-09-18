plugins {
    id("dailyfocus.android.application")
    id("dailyfocus.android.compose")
    id("dailyfocus.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.dailyfocus"
    defaultConfig {
        applicationId = "com.example.dailyfocus"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel2api35") {
                    device = "Pixel 2"
                    apiLevel = 35
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

dependencies {
    implementation(projects.features.tasks)
    implementation(projects.features.today)
    implementation(projects.features.dashboard)
    implementation(projects.features.notes)
    implementation(projects.features.focustimer)
    implementation(projects.features.calendar)
    implementation(projects.features.projects)
    implementation(projects.features.habits)
    implementation(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.model)

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.bundles.navigation3)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
