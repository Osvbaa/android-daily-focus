# Topología vigente

Revisada: 2026-09-17. Decisiones: [ADR-012](../adr/ADR-012-repository-persistence-boundary.md), [ADR-016](../adr/ADR-016-features-closure-activity-loop.md), [ADR-019](../adr/ADR-019-local-ai-and-dormant-modules.md).

```text
:app → :features:{tasks,notes,today,projects,habits,focustimer,calendar,dashboard}
     → :core:{data,model,common,ui,designsystem} y :ai según uso
:core:data → :core:database, :core:model, :core:common
:core:database → :core:model
:ai → :core:model
:core:testing → contratos/fakes para pruebas
```

La lista es de responsabilidades, no significa que cada feature declare todas esas dependencias. `settings.gradle.kts` y cada `build.gradle.kts` son la fuente ejecutable del grafo. `:app` ensambla navegación y DI; ninguna feature depende de otra. `:core:model` es JVM puro. Solo `:core:data` accede a DAOs de `:core:database`; las features consumen sus interfaces de repositorio. `:ai` encapsula la inferencia local.

## Paquetes de feature

Cada feature Gradle puede tener `domain/`, `data/` y `ui/` **como paquetes**, sin crear tres submódulos. Las reglas de dominio transversales y modelos usados por varias features viven en `:core:model`; repositorios y coordinación persistente compartidos en `:core:data`. Un paquete local solo se crea si hay código que lo necesita. El ViewModel y los estados pertenecen a `ui/`; la composición visual es responsabilidad del propietario del producto en esta renovación.

## Límites

Rutas solo con IDs/primitivas; datos se recuperan del repositorio. Las entidades Room nunca cruzan hacia features. Los efectos de notificación y servicio siguen los mismos repositorios que la UI. Migraciones son aditivas y probadas; `AppDatabase` sigue siendo única.

Calendar y Dashboard continúan hasta que una spec demuestre equivalencia y pruebe la migración de rutas hacia Today. No se eliminan por semejanza visual.
