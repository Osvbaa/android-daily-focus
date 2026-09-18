# ADR-011: APIs experimentales aisladas en superficies de Tasks

Estado: Aceptado

Fecha: 2026-09-10

## Contexto

La slice de Tasks necesita list-detail adaptativo, conocimiento del dispositivo de entrada y estilos de componentes custom. Material 3 Adaptive/Expressive, MediaQuery y Compose Styles evolucionan con mayor rapidez que las APIs estables.

## Decisión

Se fijan sus versiones en `gradle/libs.versions.toml`. Los opt-ins quedan limitados a `:features:tasks` y a componentes custom (tarjeta y selector radial); los componentes Material conservan su API estándar. La navegación adaptativa usa Navigation 3 `SceneStrategy`. La lista temporal no migra a Grid.

Roborazzi continúa como fuente de regresión visual. Según [ADR-015](ADR-015-reviewed-local-screenshot-goldens.md), los goldens se generan explícitamente en local, se comparan y revisan antes de versionarlos; CI solo ejecuta verificación.

## Consecuencias

Una actualización de Compose exige recompilar y revisar visualmente Tasks. Ninguna feature adicional adopta estas APIs por transitividad y la funcionalidad esencial sigue disponible mediante checkbox, overflow y botones accesibles.
