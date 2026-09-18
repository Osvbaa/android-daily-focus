enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.android.application") version "9.3.1" apply false
    id("com.android.library") version "9.3.1" apply false
    id("org.jetbrains.kotlin.jvm") version "2.4.20" apply false
    id("com.autonomousapps.build-health") version "3.19.1"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DailyFocus"

include(":app")
include(":ai")

include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:data")
include(":core:testing")

include(":features:calendar")
include(":features:dashboard")
include(":features:focustimer")
include(":features:habits")
include(":features:projects")
include(":features:notes")
include(":features:tasks")
include(":features:today")
include(":core:ui")
