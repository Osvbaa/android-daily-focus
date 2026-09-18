import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.roborazzi) apply false
}

val detektConfig = rootProject.file("config/detekt/detekt.yml")
val detektBaseline = rootProject.file("config/detekt/detekt-baseline.xml")

subprojects {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
}

allprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<DetektExtension> {
            config.setFrom(detektConfig)
            baseline = detektBaseline
            buildUponDefaultConfig = true
            ignoreFailures = false
        }
        tasks.withType<Detekt>().configureEach {
            reports {
                html.required.set(true)
                xml.required.set(true)
                sarif.required.set(true)
                md.required.set(false)
            }
        }
    }
}

tasks.register("checkQuality") {
    group = "verification"
    description = "Runs all local quality gates."
    dependsOn(tasks.named("detekt"))
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "detekt" }.toList() })
    dependsOn(":buildHealth")
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "lintDebug" }.toList() })
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "testDebugUnitTest" }.toList() })
    // :core:model is JVM-only and has a `test` task, not `testDebugUnitTest`.
    dependsOn(":core:model:test")
    dependsOn(":core:testing:testDebugUnitTest")
    dependsOn(":features:tasks:verifyRoborazziDebug")
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                // Existing modules have a reviewed dependency inventory in the
                // generated report. Promote this to fail after that inventory
                // is migrated; new modules can opt into fail immediately.
                severity("warn")
            }
        }
    }
}
