package com.example.dailyfocus.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    val compileSdkVer = libs.findVersion("compileSdk").get().requiredVersion.toInt()
    val minSdkVer = libs.findVersion("minSdk").get().requiredVersion.toInt()

    commonExtension.apply {
        compileSdk = compileSdkVer

        defaultConfig.apply {
            minSdk = minSdkVer
        }

        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        // Configuración de alineación de memoria (16 KB ELF Alignment) para Android 15/16
        packaging.apply {
            jniLibs.apply {
                useLegacyPackaging = false
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview"
            )
        }
    }
}
