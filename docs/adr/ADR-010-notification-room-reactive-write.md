ADR-010: Escritura Reactiva en Room desde Acciones de Notificación sin UI Activa
Estado: Aceptado

Fecha: 2026-09-07

Módulos afectados: :core:database, :core:data, :feature:tasks, :feature:pomodoro, :app

Contexto
El requisito H-4 del PRD exige que al menos el 40% de las interacciones se realicen fuera de la UI completa mediante notificaciones interactivas (ej. presionar "Completar Tarea", "Posponer para Mañana", o "Pausar Pomodoro"). Lanzar una Activity transparente o forzar el inicio de Compose en segundo plano degrada la memoria y genera lag visual. Las acciones deben escribir directamente en la base de datos de manera atómica, garantizando que si la UI está abierta o minimizada, esta reaccione en tiempo real.

Decisión
Despacho mediante BroadcastReceiver:

Las acciones directas de las notificaciones invocan un BroadcastReceiver no exportado (TaskNotificationReceiver).

El receptor utiliza goAsync() para ejecutar de forma segura una corrutina en segundo plano sin ser terminado prematuramente por el sistema operativo.

Inyección Directa del Repositorio:

Vía Hilt (@AndroidEntryPoint), el BroadcastReceiver inyecta directamente la interfaz TaskRepository o PomodoroRepository.

Ejecuta la mutación directamente sobre la capa de datos (taskRepository.completeTask(taskId)).

Sincronización Automática con la UI:

Al mutar Room directamente, los Flow<List<Task>> observados activamente por los ViewModels en Compose emiten automáticamente el nuevo snapshot de datos gracias al mecanismo reactivo de SQLite de Room 3.

La UI y el widget se actualizan solos, sin acoplamientos ni llamadas manuales de sincronización.

Analítica Desacoplada:

El BroadcastReceiver despacha el evento de analítica correspondiente indicando surface = NOTIFICATION_ACTION.

Consecuencias
Positivas: Respuesta de interacción inferior a 50 milisegundos para el usuario desde la barra de estado; consumo mínimo de batería y memoria RAM; cero riesgo de ActivityNotFoundException.

Negativas: Se deben contemplar escenarios donde la base de datos esté ocupada o en proceso de migración al recibir el intent.

Verificación: Pruebas instrumentadas y unitarias sobre el BroadcastReceiver usando FakeTaskRepository validando la invocación del método de compleción y cierre ordenado de goAsync().