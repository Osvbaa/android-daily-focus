# ADR-012: Repositorios como frontera de persistencia de las features

## Status

Accepted — aprobado explícitamente por el usuario el 2026-09-12.

Fecha: 2026-09-12.

Actualiza parcialmente [ADR-002](ADR-002-centralized-app-database.md).
No sustituye sus garantías de base única, integridad referencial ni migraciones.

## Context

[La topología](../architecture/system_topology.md) prohíbe dependencias de las
features hacia `:core:database`, pero ADR-002 y la regla 5 de `AGENTS.md` indican
inyección de DAOs en features. `TaskRepository` y `OfflineFirstTaskRepository`
ya implementan la frontera descrita por la topología. Esta contradicción debe
resolverse antes de ampliar el contrato de Tasks.

Además, ADR-002 describe Room 3, mientras `gradle/libs.versions.toml` fija Room
`2.8.5`. `DatabaseModule` ya utiliza `BundledSQLiteDriver`. La versión del esquema
de `AppDatabase` es 2; no equivale a la versión de la biblioteca.

## Decision

Se adopta:

- Las features y sus receptores consumen interfaces de repositorio de
  `:core:data`, mediante Hilt. No importan DAOs, entidades ni APIs de Room.
- `:core:data` coordina reglas y transacciones; consume los DAOs granulares de
  `:core:database`. Esta última sigue siendo dueña de la única `AppDatabase`.
- Los modelos y resultados compartidos son Kotlin puro en `:core:model`.
  La UI recibe errores tipados, no mensajes procedentes de excepciones SQL.
- `:app` conecta destinos mediante IDs primitivos; no se permiten dependencias
  entre features, ni siquiera para compartir captura o acciones sobre tareas.
- El cierre conserva Room `2.8.5` y el driver ya configurado. No incluye una
  migración de biblioteca a Room 3 ni actualizaciones oportunistas del stack.
  La ampliación del esquema sí tendrá migración no destructiva y pruebas.

Las cláusulas de ADR-002, `AGENTS.md` y la topología se alinean con esta decisión.
Las menciones históricas a Room 3 no autorizan una actualización de biblioteca.

## Consequences

Todas las superficies comparten validación, conflictos y atomicidad. Las reglas
de Tasks no se duplican en Dashboard ni en receptores. Se conservan las APIs
granulares; no se crea una capa genérica de casos de uso por cada método.

Los guardrails deben comprobar el grafo Gradle además de los imports: ausencia
de feature→feature y feature→database/network, dominio sin framework y ausencia
de entidades en firmas públicas consumidas por features. Los tests de
integración de datos sí pueden utilizar Room real.
