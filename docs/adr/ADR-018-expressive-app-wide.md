# ADR-018: Material 3 Expressive y continuidad de navegación

## Status

Accepted — 2026-09-17, por aprobación explícita del propietario. Sustituye únicamente la restricción de alcance Expressive de ADR-011.

## Context

La solicitud de producto del 2026-09-17 pide renovar todas las pantallas con
Material 3 Expressive, una barra flotante, prioridades diferenciadas y continuidad
entre captura, planificación y enfoque. El loop está descrito en
[01-core-loop.md](../product/01-core-loop.md).

[ADR-011](ADR-011-experimental-compose-task-surfaces.md) limita los opt-ins
experimentales a Tasks e impide su adopción transitiva por otras features.
Aplicar un tema Expressive global requiere ampliar explícitamente esa decisión.

La inspección inicial encontró:

- `DailyFocusTheme` utiliza MaterialTheme, sin política de movimiento compartida.
- `MainAppStructure` presenta una NavigationBar convencional; `selectRoot`
  borra el back stack al cambiar de destino principal.
- `AppNavigation` reemplaza Más al abrir sus destinos: volver no conserva ese
  punto de entrada. No declara una política propia de transiciones.
- Hoy muestra identificadores de proyecto y coloca todos los selectores de
  proyecto en una Row; requiere nombres legibles y adaptación a anchuras menores.
- Proyectos mantiene el formulario siempre visible y coloca el detalle después
  de toda la lista; las tareas del detalle son texto sin acción de apertura.
- Calendario es una columna de tareas de hoy sin desplazamiento ni selección
  de fecha. No debe presentarse como un calendario completo hasta implementarlo.

Estos hallazgos son de lectura de código, no de una evaluación en dispositivo.
La revisión completa de las demás pantallas sigue pendiente.

## Decision

Se propone ampliar únicamente la restricción de alcance Expressive de ADR-011:

1. `:core:designsystem` posee el tema Expressive, formas, roles de color y
   movimiento compartido. Encapsula los opt-ins necesarios en sus implementaciones;
   sus consumidores no reciben anotaciones experimentales por conveniencia.
2. `:core:ui` posee componentes compartidos que interpretan modelos de dominio,
   incluyendo prioridades. El design system continúa sin entidades de negocio.
3. `:app` compone la barra flotante y coordina navegación entre features mediante
   callbacks e IDs primitivos. No se introducen dependencias entre features.
4. Las APIs experimentales adicionales en features requieren uso localizado y
   justificación; esta decisión no habilita indiscriminadamente Styles, MediaQuery
   u otras APIs experimentales. Se mantienen las restricciones restantes de ADR-011.
5. Primero se verifica la disponibilidad de las APIs en las versiones fijadas.
   Cualquier cambio de versión debe documentar compatibilidad e impacto; no se
   añade una biblioteca de animación externa.

La compilación confirmó que `MaterialExpressiveTheme` es interno en la versión
resuelta actualmente. La implementación inicial mantiene `MaterialTheme` público
y aplica formas redondeadas, elevación tonal y movimiento Compose en componentes
de la aplicación. La adopción de `MaterialExpressiveTheme` queda pendiente de una
versión pública compatible verificada; no se invoca una API interna.

### Resultado de UX propuesto

- Paleta índigo, pizarra y menta coherente con el sistema documentado, en claro y
  oscuro. Prioridades con colores semánticos, texto e iconos; nunca solo color.
- Barra flotante con destinos etiquetados, selección visible, zonas táctiles de
  al menos 48 dp y adaptación al teclado, insets, fuente grande y pantalla ancha.
- Encabezados con fecha, contexto y progreso real; captura rápida accesible sin
  desplazar formularios largos. El teclado no debe ocultar guardar o cancelar.
- Transiciones de navegación direccionales y reversibles, cambios de tamaño y
  forma ligados a acciones, animación de progreso y reordenación con claves
  estables. Respetar la escala de animación del sistema y predictive back.
- Hoy → tarea → enfoque; nota → tareas extraídas; proyecto → sus tareas. Volver
  conserva contexto y borradores según los contratos vigentes.
- Auditoría por pantalla de carga, vacío, error con recuperación, guardado,
  accesibilidad y funcionamiento local antes de dar por terminado cada flujo.

### Secuencia de implementación

1. Completar inventario de pantallas, estados, pruebas y requisitos por feature.
2. Tema y navegación global; pruebas de selección y retorno entre destinos.
3. Hoy y Tareas: captura, prioridad, detalle y comienzo de enfoque.
4. Notas: captura, guardado y revisión de tareas extraídas.
5. Proyectos: listado, detalle, hitos y navegación a tareas.
6. Hábitos, Focus, Calendario, Dashboard y Más, una slice cada vez, preservando
   sus contratos de persistencia y de actividad.

Cada slice incluye pruebas de comportamiento con Fakes. Se conserva AppDatabase,
la frontera de repositorios, UDF y las colecciones inmutables. No se modifica la
política de actividad, puntos, finalización del timer o IA mediante este ADR.
Cualquier conflicto de esos contratos necesita su propia decisión.

## Consequences

La experiencia visual puede ser coherente en toda la aplicación sin duplicar
infraestructura. El coste de revisar actualizaciones de Compose pasa de Tasks
al sistema visual compartido y a todas las pantallas consumidoras.

La implementación requiere ejecutar `checkQuality` (o todos los comandos
equivalentes de AGENTS.md), verificar capturas y revisar recorridos en dispositivo,
modo oscuro, fuentes grandes, teclado y diferentes anchuras. No se debilitan
pruebas ni se reemplazan goldens automáticamente para ocultar diferencias.
Los nuevos goldens se generan explícitamente y se revisan según ADR-015.

La aprobación permite implementar las slices descritas sin cambiar los contratos de persistencia.

## References

- [Material 3 en Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [MaterialExpressiveTheme](https://developer.android.com/reference/kotlin/androidx/compose/material3/MaterialExpressiveTheme.composable)
- [Sistema visual existente](../design/system_and_states.md)
- [Cierre del loop local](ADR-016-features-closure-activity-loop.md)
