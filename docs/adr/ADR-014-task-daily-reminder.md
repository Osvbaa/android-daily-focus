# ADR-014: Recordatorio diario opcional y acciones de Tasks

## Status

Accepted — aprobado explícitamente por el usuario el 2026-09-12.

Fecha: 2026-09-12.

Amplía [ADR-010](ADR-010-notification-room-reactive-write.md); requiere
[ADR-012](ADR-012-repository-persistence-boundary.md) y
[ADR-013](ADR-013-durable-task-drafts-and-undo.md).

## Context

El cierre incluye recordatorios por fecha, no hora por tarea, RemoteInput,
Pomodoro ni widgets. El modelo actual conserva fechas civiles en epoch days.
Se necesita una política de publicación además del receptor descrito en ADR-010.

## Decision

- Recordatorio desactivado inicialmente. Al habilitarlo se ofrece una hora local
  editable, inicialmente 09:00, y se solicita permiso de notificaciones desde
  una acción explícita. Denegarlo no bloquea tareas ni dispara nuevas solicitudes
  automáticas. Si el canal está deshabilitado se ofrece abrir sus ajustes.
- Persistir configuración local y fecha de última publicación. Usar WorkManager
  ya presente en el catálogo para trabajo único y recuperable. Recalcular la
  siguiente ejecución al cambiar hora, fecha, zona, configuración o reiniciar.
- La hora es aproximada: el sistema puede retrasar el trabajo. No usar alarmas
  exactas ni foreground service para garantizar puntualidad del resumen.
- Consultar pendientes de la fecha local al ejecutar. No emitir resúmenes vacíos,
  no reproducir resúmenes de días perdidos y no alertar dos veces el mismo día.
  Desactivar cancela el trabajo y las notificaciones de este recordatorio.
- Mostrar un resumen agrupado y notificaciones hijas accionables por tarea,
  ordenadas de forma determinista. Publicar silenciosamente las hijas y alertar
  solo con el resumen. Ocultar el contenido de tareas en la pantalla bloqueada.
- Acciones: completar y posponer a mañana. Si hay subtareas pendientes, usar
  «Completar todas» en vez de completar silenciosamente el agregado. El
  repositorio revalida esa condición al ejecutar: una notificación obsoleta no
  otorga permiso implícito para completar nuevas subtareas.
- Tras completar, ofrecer Deshacer con el ID y vencimiento de ADR-013. Que una
  notificación siga visible no extiende la validez de la operación; una acción
  vencida se rechaza sin modificar datos y actualiza el aviso.
- Las acciones de mutación utilizan un receptor explícito no exportado y
  `PendingIntent` inmutable con identidad distinta por tarea/acción. Validar
  acción e IDs y tolerar duplicados, tareas eliminadas y errores de persistencia.
- `goAsync()` se cierra en `finally`. No mantener el receptor vivo esperando la
  ventana de Deshacer ni trasladar la operación a un ViewModel. La recuperación
  analítica corresponde al mecanismo durable de ADR-013.
- Tocar el contenido abre directamente la Activity con ID primitivo y destino
  editor. No usar un receptor como trampolín para lanzar la Activity.
- Las demás superficies reaccionan a Room. La analítica identifica
  `NOTIFICATION_ACTION`; no se registra una mutación que no se haya confirmado.

## Consequences

No se amplía la fecha de vencimiento a timestamp ni se solicitan privilegios de
alarmas exactas. Se conserva `TaskNotificationReceiver` en `:features:tasks` y
la orquestación de destinos en `:app`. No se garantizan avisos durante force-stop
ni puntualidad exacta frente a restricciones del sistema.

Verificar con fakes la planificación, la fecha de consulta, permisos, duplicados,
fallos y cierre del trabajo asíncrono. Verificar en dispositivo publicación,
acciones, navegación, canal bloqueado, proceso sin UI y cambios de zona/fecha.
Las comprobaciones de estado en el repositorio son obligatorias aunque la UI
de notificación ya haya filtrado o validado la tarea.
