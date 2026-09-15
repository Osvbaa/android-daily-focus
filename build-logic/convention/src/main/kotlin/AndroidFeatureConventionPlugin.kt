package com.example.dailyfocus.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("dailyfocus.android.library")
                apply("dailyfocus.android.compose")
                apply("dailyfocus.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:designsystem"))

                // Navigation & Lifecycle para Compose
                add("implementation", libs.findBundle("navigation3").get())
                add("implementation", libs.findLibrary("androidx-hilt").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}