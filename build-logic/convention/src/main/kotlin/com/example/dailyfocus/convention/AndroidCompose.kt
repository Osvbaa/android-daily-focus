package com.example.dailyfocus.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: ApplicationExtension) {
    extension.buildFeatures { compose = true }
    configureComposeDependencies()
}

internal fun Project.configureAndroidCompose(extension: LibraryExtension) {
    extension.buildFeatures { compose = true }
    configureComposeDependencies()
}

private fun Project.configureComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.findLibrary("compose-material3").get())
        add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
        add("implementation", libs.findLibrary("compose-material-icons-extended").get())
    }
}
