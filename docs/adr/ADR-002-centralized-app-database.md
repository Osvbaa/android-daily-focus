ADR-002: Persistencia Centralizada (AppDatabase en :core:database) con DAOs Desacoplados
Estado: Aceptado

Actualizado parcialmente por [ADR-012](ADR-012-repository-persistence-boundary.md)
el 2026-09-12: frontera de repositorios y versión de Room del catálogo.

Fecha: 2026-09-07

Módulos afectados: :core:database, :core:data, :feature:*

Contexto
En arquitecturas multimódulo de Android existen dos estrategias de persistencia con Room:

Opción A (Bases de datos distribuidas): Cada :feature:X posee su propio archivo .db SQLite y base de datos independiente.

Opción B (Base de datos unificada): Un único AppDatabase en :core:database, exponiendo DAOs granulares a :core:data y repositorios a cada feature.

El producto requiere integridad referencial estricta: si una nota se elimina, las filas de cruce en TaskNoteCrossRef deben borrarse en cascada (ON DELETE CASCADE), pero la tarea debe sobrevivir. Esto es inviable con bases de datos SQLite separadas sin sincronizaciones complejas en memoria propensas a corrupción.

Decisión
Adoptar Opción B: Un único AppDatabase monolítico alojado en :core:database.

Las features jamás conocen AppDatabase, DAOs ni entidades SQLite. Consumen interfaces de repositorio de :core:data inyectadas mediante Hilt. Solo :core:data consume TaskDao y NoteDao.

Las relaciones entre agregados de diferentes features se modelan mediante tablas intermedias de IDs primitivos (claves foráneas simples), prohibiendo anotaciones @Relation o @Embedded que acoplen clases de entidad entre módulos.

Se utiliza la versión de Room fijada en gradle/libs.versions.toml (2.8.5 al aceptar ADR-012), con BundledSQLiteDriver() para desacoplar SQLite del sistema operativo. No se incluye una migración a Room 3 en el cierre de Tasks.

Consecuencias
Positivas: Integridad transaccional ACID garantizada por el motor SQLite; borrado en cascada real; una sola conexión y pool de threads para la base de datos.

Negativas: La modificación de cualquier entidad requiere regenerar el esquema global en :core:database y recompilar dicho módulo.

Verificación: Konsist asegura que solo :core:database importe androidx.room.RoomDatabase y que ningún módulo :feature:* tenga dependencias directas contra AppDatabase.
