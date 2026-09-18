# ADR-019: IA local productiva y retiro de infraestructura sin consumidores

Estado: Aceptado para la renovación SDD aprobada por el usuario, 2026-09-17. Sustituye la implementación prevista en [ADR-003](ADR-003-hybrid-ai-extraction-engine.md); no borra su contexto histórico. Compatible con [ADR-017](ADR-017-koog-litert-spike.md).

## Contexto

ADR-003 describe `:core:ai` con LiteRT y fallback Ktor/SSE. El producto implementa `:ai` con ML Kit Prompt API y Gemini Nano. `:core:network`, `:sync`, `:integrations:google` y `:core:analytics` no contienen flujos productivos; sus módulos vacíos o placeholders añaden complejidad al grafo. La analítica local reside en `:core:common` y su tracker actual no envía eventos.

## Decisión

- Mantener `:ai` como adaptador local de `AiExtractionEngine`; sugerencias requieren confirmación antes de persistirse. La indisponibilidad se comunica como resultado tipado y no impide capturar manualmente.
- Retirar los módulos sin consumidores productivos anteriores del grafo Gradle y sus fuentes placeholder. Quitar dependencias hacia ellos. No retirar datos de Room ni migraciones por esta limpieza.
- Sincronización remota, cloud fallback, Koog/LiteRT y analítica remota son capacidades propuestas. Antes de implementarlas se requieren spec, ADR, contrato de privacidad y pruebas; no se reserva un módulo vacío como promesa.
- Conservar `:core:common` mientras aloje dispatchers, reloj y contrato de analítica usados. `:core:testing` conserva fakes y guardrails.

## Consecuencias y verificación

El grafo se reduce sin alterar el almacenamiento local. Validar búsqueda de referencias, compilación de `:app`, tests de IA y `checkQuality`. El futuro fallback de IA requiere otro ADR que resuelva privacidad, autenticación, costo y comportamiento offline.
