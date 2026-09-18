# DailyFocus — PRD del Core Loop y Diferenciadores

> Documento histórico de ideación. Sus fases, motores IA y vínculos propuestos no describen necesariamente el producto actual. Consultar [alcance vigente](00-prd.md) y [specs](../specs/README.md).
### Fase 1 del Pipeline Maestro v7 — de la visión de producto a la fase que la implementa

Este documento captura las decisiones de producto validadas hasta ahora. Cada elemento indica en qué fase del pipeline entra, para que quede trazable y nadie lo arquitecte antes de tiempo.

---

## El Core Loop

```
   NOTAS ──extracción IA (1 toque)──► TAREAS ──┬──vinculación a meta──► PROYECTO (hitos IA)
(captura libre)                (acción diaria) │
                                                └──ejecución con foco──► POMODORO ──alimenta racha──► HÁBITOS
                                                                    (sesión activa)          (constancia elástica)
```

Reglas de interconexión, y su fase:

| Conexión | Mecánica | Fase |
|---|---|---|
| Nota → Tarea | `[ ]` o texto resaltado despliega chip "Convertir a Tarea"; botón de Edge AI extrae pendientes con fecha tentativa sin salir del editor | MUST — vertical slice de Notas (Fase 8), motor en `:core:ai` (ADR-003) |
| Tarea ↔ Pomodoro | Micro-botón ▶ en la tarea precarga una sesión de Pomodoro con esa tarea; al sonar, un solo tap pregunta si se completó o si necesita otra sesión | SHOULD — vertical slice de Pomodoro, requiere `taskId: String?` en `PomodoroSession` (ADR-004) |
| Pomodoro → Hábito | Un pomodoro completado alimenta automáticamente un hábito de "Foco Profundo"/"Estudio Diario" sin doble marcado | COULD — `HabitPomodoroCrossRef` en `:core:database`, validado en producto (Apéndice A del pipeline) |
| Proyecto → Tareas | El usuario da una meta ("Aprender TCP/IP en 30 días"); la IA desglosa en 4 hitos y 15 tareas atómicas inyectadas al backlog | COULD — requiere `:core:ai` con prompt de descomposición estructurada, su propio ADR cuando entre en alcance |

**Nota de disciplina de fases**: el Core Loop completo (los 4 vínculos) no es MUST — el MoSCoW de la Fase 1 ya estableció que el MUST es Tareas+Notas+extracción IA. Los otros tres vínculos se activan cuando su feature respectiva entra en su vertical slice, no antes.

---

## Diferenciadores por feature

### Hábitos — "Hábitos Elásticos"
Tres niveles de cumplimiento diario por hábito (Mini / Plus / Elite, ej. 5 flexiones / 20 min / 1 hora). Deslizar horizontalmente sobre el hábito elige el nivel completado. La racha nunca se rompe si se cumple al menos Mini.
- **Fase 4 (ADR)**: modelar `HabitCompletionLevel` como enum en el dominio, no como booleano — el streak se calcula contra "¿se cumplió algún nivel?", no contra un único umbral.
- **Fase 8**: UI del gesto de deslizamiento horizontal con selección de nivel.

### Tareas — gestos de triaje
Deslizar una tarea a la izquierda abre un menú radial (Hoy / Mañana / Este fin de semana / Próxima semana); soltar sobre la opción reprograma al instante. Subtareas con barra de progreso dinámica en la tarjeta principal.
- **Fase 2**: el menú radial es una pieza de diseño custom, no un componente M3 estándar — diseñarlo aquí antes de implementarlo.
- **Fase 8, con secuenciación explícita**: el radial de gestos se saca del MUST de la primera vertical slice (ver nota arriba) — se construye en una segunda pasada sobre `:feature:tasks`, no bloquea el core loop.

### Pomodoro — "Flow State" adaptativo
Si el usuario no toca el teléfono al llegar a los 25 minutos, suena alarma de duración corta — entra en Overtime silencioso con una notificación sutil ("+10 min de foco extra, ¿extender o pausar?").
- **Fase 1.5**: cubierto por el Spike 1 (Foreground Service + Doze) ya definido — el Overtime necesita el mismo cálculo de deltas contra `SystemClock.elapsedRealtime()`.
- **Fase 4 (ADR)**: máquina de estados del timer (`Running`/`Overtime`/`Paused`/`Completed`) es candidata real a MVI estricto (reducer puro) — es exactamente el caso de "concurrencia compleja de eventos" que ADR-001 reserva para MVI.

### Notas — scratchpad con FTS5 y transclusión
Bloques Markdown rápidos; búsqueda semántica local; mencionar `@Tarea-12` en una nota refleja el estado de esa tarea en tiempo real.
- **Fase 4 (ADR)**: FTS5 vía Room (`@Fts4`/virtual table) para búsqueda; la transclusión es un `Flow` que combina el contenido parseado de la nota con `TaskDao.observeTask(id)` — factible de forma limpia gracias a Opción B (`AppDatabase` única, Fase 4 ADR-002).
- **Fase 8**: parsing de `@Tarea-N` en el editor es UI, no dominio — vive en `:feature:notes:ui`.

---

## Notificaciones interactivas (API 36)

**Tarea pendiente** — acciones directas sin abrir la app:
```
🔔 Tarea Pendiente — Preparar reporte de arquitectura
[ ✔ Completar ]  [ ⏱ Iniciar Pomodoro ]  [ ↩ Posponer ]
                  └─► [Mañana] [+2 horas] [Texto libre (RemoteInput)]
```
- `Completar` → `BroadcastReceiver`/`WorkManager` actualiza Room inmediatamente, con respuesta háptica.
- `Iniciar Pomodoro` → lanza el foreground service sin levantar la UI.
- `Posponer` → chips rápidos o `RemoteInput` de texto libre.

**Pomodoro activo** — notificación persistente (`CATEGORY_STOPWATCH`):
```
🔔 Pomodoro Activo — Implementando: Capa de Red con Ktor [18:42 restantes]
[ ⏸ Pausar ]   [ ⏹ Terminar ]   [ +5 min ]
```

**Ruta en el pipeline**: esto es la instrumentación concreta de la Fase 8, paso 9 (UI/notificaciones) de las vertical slices de Tareas y Pomodoro — pero el patrón de `RemoteInput` + `BroadcastReceiver` 
actualizando Room directamente (sin pasar por el ViewModel en pantalla) es una decisión de arquitectura real: **candidato a ADR-010** — cómo una acción de notificación escribe al repositorio sin que exista una UI activa observándolo. 
Vale la pena registrarlo antes de implementar la primera notificación accionable, para que el patrón sea consistente entre Tareas y Pomodoro.

---

## Pendiente de profundizar (según lo pediste)

Quedan abiertas, para seguir iterando cuando quieras: mejoras adicionales al Pomodoro más allá de Flow State (qué lo hace más que un cronómetro), más gestos por feature, y el desglose de metas de Proyecto con IA. Ninguna bloquea el MUST actual.
