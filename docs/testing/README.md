# Testing strategy

Nivel 1: lógica/estado (Turbine + Fakes) 
Nivel 2: persistencia/migraciones (MigrationTestHelper) 
Nivel 3: regresión visual (Roborazzi, goldens solo desde CI) 
Nivel 4: estático (Konsist, dependency-analysis-gradle-plugin, checksums)

Fakes viven en `:core:testing`. `MockK`/`Mockito` prohibidos — ver `AGENTS.md` Regla 4.
