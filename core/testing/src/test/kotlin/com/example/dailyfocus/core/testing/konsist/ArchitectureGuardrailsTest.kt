package com.example.dailyfocus.core.testing.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureGuardrailsTest {
    @Test fun noFilesShouldImportMockitoOrMockk() {
        Konsist.scopeFromProject().files.assertFalse { file ->
            file.hasImport { it.name.startsWith("org.mockito") || it.name.startsWith("io.mockk") }
        }
    }

    @Test fun coreModelFilesMustNotImportAndroidFrameworkPackages() {
        Konsist.scopeFromModule("core:model").files.assertFalse { file ->
            file.hasImport { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
        }
    }

    @Test fun viewModelsMustResideInFeatureModules() {
        Konsist.scopeFromProject().classes().withNameEndingWith("ViewModel").assertTrue {
            it.resideInPackage("..features..") || it.resideInPackage("..feature..")
        }
    }

    @Test fun featureModulesMustNotImportOtherFeatures() {
        Konsist.scopeFromProject().files.filter { "/features/" in it.path.replace('\\', '/') }.assertFalse { file ->
            val ownFeature = file.path.replace('\\', '/').substringAfter("/features/").substringBefore("/")
            file.imports.any { import -> ".features." in import.name && !import.name.contains(".features.$ownFeature.") }
        }
    }
}

