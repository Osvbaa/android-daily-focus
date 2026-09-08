# PRD: DailyFocus (Productivity & Focus Engine)

## 1. Problema central

**Fricción de captura y mantenimiento**: registrar una idea o tarea toma demasiados toques, lo que lleva al abandono; mantener el sistema (organizar, actualizar) se vuelve una carga en sí misma.

**Desconexión entre pensamiento y acción**: las ideas anotadas en reuniones o estudio quedan enterradas como texto plano y nunca se traducen en tareas accionables.

**Rigidez de los sistemas de hábitos ("streak shame")**: el tracking binario desmotiva cuando un imprevisto impide completar la meta al 100%.

**Carga cognitiva y procrastinación**: sensación de sobrecarga sin poder organizar mentalmente, dificultad para empezar aunque se sepa qué hacer, culpa y ansiedad por lo pendiente — especialmente agudo en personas con TDAH, alta carga laboral o vida ajetreada.

**Falta de foco guiado**: las listas de tareas no ofrecen un mecanismo integrado para sentarse y ejecutarlas de inmediato.

**Gaps de la competencia**: paywall agresivo en el plan gratuito; interfaces densas con curva de aprendizaje alta; degradación de la organización cuando crece el volumen de tareas/notas; sistemas de notificación deficientes (agresivos o insuficientes).

## 2. Visión de producto

*(No es lo que la hipótesis mide — es el rumbo del producto completo)*: un sistema donde capturar notas, tareas, hábitos y proyectos, vincularlos entre sí y ejecutarlos en bloques de trabajo estructurados no genera carga cognitiva — el flujo entre features es coherente, útil y fluido.

## 3. Hipótesis de producto (quirúrgica)

**Creemos que** las personas con proyectos dispersos entre notas y tareas lograrán mantener constancia si pueden capturar y triajar en segundos, y convertir sus notas en tareas accionables con un toque de IA.

**Lo sabremos cuando:**
1. El **35%** de las tareas nuevas se generen desde extracción asistida por IA a partir de notas.
2. La **retención D30** supere el **28%**.
3. El tiempo de captura y triaje inicial sea **inferior a 4 segundos** *(indicador de UX/performance, no de la hipótesis en sí — se mide igual, informa si el producto es lo bastante rápido para que la hipótesis tenga oportunidad de cumplirse)*.
4. **Al menos el 40%** de las interacciones diarias (completar, posponer, crear) ocurren fuera de la pantalla completa — vía widget, notificación accionable o gesto — **sin que esto reduzca la retención D30 del punto 2**. *(Corregido: "menos tiempo en la app" por sí solo es ambiguo — puede ser eficiencia o abandono. Emparejarlo con retención lo hace falsable.)*

## 4. Delimitación MoSCoW

### MUST HAVE (MVP — valida la hipótesis central)
- CRUD de Tareas: creación ultra-rápida, fecha de vencimiento, prioridad, estados, reprogramación simplificada (Hoy/Mañana/Próxima semana).
- CRUD de Notas (Markdown básico): checklist `[ ]`; resaltar texto muestra un chip para crear una tarea desde ese fragmento.
- Extracción de tareas con IA (on-device / cloud fallback): botón contextual que analiza la nota y sugiere tareas para confirmación con un toque.
- Enlace Tarea ↔ Nota: registro del origen de la tarea vía ID primitivo (`TaskNoteCrossRef`).
- Persistencia offline-first: Room local como SSOT al 100%.
- Dashboard "Mi Día": notas recientes + tareas programadas para hoy. *(Corregido: sin confirmación de hábitos — Hábitos es COULD, no puede tener una acción de Dashboard en el MUST.)*
- Contrato de analítica de producto: `task_created`, `note_ai_extracted` instrumentados desde el día uno.

### SHOULD HAVE (segunda vertical slice)
- Pomodoro con Live Notification: Foreground Service con cálculo monotónico de tiempo, controles directos en notificación.
- Sincronización remota: Ktor hacia backend para respaldo en la nube.

### COULD HAVE (tercera vertical slice y expansión)
- Hábitos Elásticos: 3 niveles diarios (Mini/Plus/Elite) para proteger la racha.
- Módulo de Proyectos, incluyendo descomposición de objetivos con IA (hitos, tareas, fecha límite, primer paso). *(Corregido: unificada con la entrada duplicada que antes también aparecía en MUST — ningún criterio de éxito la mide todavía, y agrega un módulo completo + un prompt de IA más complejo al riesgo del MVP.)*
- Widgets de pantalla de inicio.
- Wear OS companion.
- Captura por voz.

### WON'T HAVE (fuera de alcance inicial)
- Red social o feed público de hábitos.
- Chatbot conversacional generalista sin anclaje en notas o tareas.
- Clientes web o desktop.
- Colaboración multiusuario en tiempo real.

## 5. Risk Register
- Consumo térmico de inferencia local sostenida (IA on-device).
- Muerte del timer de Pomodoro en Doze Mode — de-riesgado en el Spike 1 (Fase 1.5).
- Migraciones destructivas de esquema en Room 3.
- Expansión de alcance antes de cerrar el MUST (ver correcciones de este documento).

## 6. BDD de referencia (núcleo de la hipótesis)
```gherkin
Scenario: Generación local de tareas a partir de una nota con Edge AI
  Given el usuario visualiza una nota con más de 50 palabras
  And los pesos del modelo LiteRT están listos en local
  When solicita "Extraer Tareas Pendientes"
  Then el modelo procesa el contenido en la NPU/CPU local
  And la UI transiciona a Success mostrando las tareas detectadas para confirmación
```

Ver `docs/product/01-core-loop.md` para las reglas de interconexión entre features y su fase de implementación correspondiente.
