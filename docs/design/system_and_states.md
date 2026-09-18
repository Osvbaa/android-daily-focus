
> Referencia visual histórica para el propietario de UI. Los contratos funcionales vigentes están en `docs/specs/`; las menciones a targetSdk 36 y fases antiguas no rigen la arquitectura actual.

## 1. Tokens Material Design 3 (`:core:designsystem`)

El módulo `:core:designsystem` actúa como la biblioteca canónica de átomos visuales. No contiene lógica de negocio ni entidades de dominio.

### Paleta Tonal y Roles de Color (Material 3 Dynamic & Baseline)

Se estructura bajo un esquema de alto contraste visual y bajo esfuerzo cognitivo:

* **Primary (`md_theme_primary`):** Índigo/Cobalto Profundo (`#2D5BFF` en Light / `#A8C7FA` en Dark). Acción focal, botón primario de captura y acento en temporizador activo.
* **Secondary (`md_theme_secondary`):** Pizarra Cálido (`#565E71` en Light / `#BEC6DC` en Dark). Chips de estado, etiquetas y contadores neutrales.
* **Tertiary (`md_theme_tertiary`):** Esmeralda / Menta Foco (`#006C4C` en Light / `#70D8A5` en Dark). Estados de completitud, rachas activas y modo Flow State en Pomodoro.
* **Surface & Background:**
* En Dark Mode: `Surface` (`#111318`), `SurfaceContainer` (`#1D2024`), `SurfaceContainerHigh` (`#282A2F`).
* Sin negros absolutos (`#000000`) en superficies de lectura para evitar *black smearing* en paneles OLED durante el scroll.

* **Error:** Rojo bermellón (`#BA1A1A` / `#FFB4AB`) reservado exclusivamente para fallos destructivos o validaciones duras.

### Tipografía (Escala M3 Estricta)

Todas las dimensiones se expresan en `sp`. No se admiten tamaños arbitrarios en los composables.

| Token | Peso | Tamaño / Interlineado | Tracking | Propósito |
| --- | --- | --- | --- | --- |
| **HeadlineMedium** | SemiBold (600) | 28sp / 36sp | 0sp | Encabezados de Dashboard ("Mi Día") |
| **TitleLarge** | Medium (500) | 22sp / 28sp | 0sp | Título de notas en editor |
| **TitleMedium** | Medium (500) | 16sp / 24sp | +0.15sp | Título de tarea en tarjeta |
| **BodyLarge** | Regular (400) | 16sp / 24sp | +0.5sp | Cuerpo de notas y texto de entrada |
| **BodyMedium** | Regular (400) | 14sp / 20sp | +0.25sp | Subtareas y metadatos |
| **LabelLarge** | SemiBold (600) | 14sp / 20sp | +0.1sp | Botones de acción, chips interactivos |
| **LabelSmall** | Medium (500) | 11sp / 16sp | +0.5sp | Timers pequeños y badges de estado |

### Formas y Elevaciones Tonal

Material Design 3 sustituye las sombras proyectadas (*drop shadows*) por elevación tonal basada en capas de color de superficie:

* **Level 0 (Flat):** 0dp — Fondo principal de pantalla.
* **Level 1 (Cards de tareas/notas):** 1dp — `surfaceContainerLow`.
* **Level 2 (Menús rápidos / Dialogs):** 3dp — `surfaceContainer`.
* **Level 3 (ModalBottomSheet / Extracción IA):** 6dp — `surfaceContainerHigh`.
* **Esquinas (Shapes):**
* Tarjetas de tarea/nota: `ShapeDefaults.Medium` (12dp).
* Chips y cápsulas de triaje: `ShapeDefaults.Full` (Pill, 50%).
* Hojas inferiores (*BottomSheets*): `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`.


## 2. Protocolo de Window Insets (Android 16 / TargetSDK 36)

Con el modo Edge-to-Edge mandatario por el sistema operativo, los composables deben consumir las barras del sistema de forma predecible sin incurrir en padding doble.

### Jerarquía de Consumo

MainActivity
  └─► enableEdgeToEdge()
        └─► Scaffold(contentWindowInsets = WindowInsets.safeDrawing)
              │
              ├─► TopAppBar (Consume automáticamente statusBars insets)
              │
              ├─► NavDisplay / Screen Content
              │     └─► LazyColumn(
              │           contentPadding = innerPadding + WindowInsets.navigationBars
              │         )
              │
              └─► InputBar / BottomSheet (Consume WindowInsets.ime + safeGestures)

### Reglas Técnicas

1. **Regla del Scaffold Raíz:** Los `Scaffold` contenedores de pantalla deben declarar explícitamente: contentWindowInsets = WindowInsets.safeDrawing

2. **Prevención de Doble Padding en Listas:** Cuando un `LazyColumn` recibe el `innerPadding` del `Scaffold`, no debe anidar modificadores adicionales de `WindowInsets.navigationBars.asPaddingValues()`. El padding se aplica exclusivamente a través del parámetro `contentPadding` de la lista, nunca mediante `Modifier.padding()` en el contenedor exterior.
3. **Manejo del Teclado (IME):** En pantallas con campos de captura rápida o el editor de notas, se debe usar `Modifier.imePadding()` en el contenedor del input inferior para que flote sobre el teclado sin empujar o romper la cabecera superior.
4. **Hojas Inferiores (`ModalBottomSheet`):** El componente de M3 gestiona sus propios insets inferiores de forma nativa. No se debe aplicar `Modifier.navigationBarsPadding()` en el contenido interno de la hoja para evitar espacios en blanco innecesarios sobre la barra de gestos.

## 3. Matriz Exhaustiva de los 11 Estados de UI

Cada pantalla del MUST (Dashboard "Mi Día", Editor de Notas y Hoja de Extracción IA) debe resolver estos 11 estados antes de pasar las pruebas unitarias y de captura visual.

| # | Estado | Dashboard "Mi Día" | Editor de Notas | Extracción IA (BottomSheet) |
| --- | --- | --- | --- | --- |
| **1** | **Initial (Uninitialized)** | Shimmer sutil de estructura (Placeholders grises neutros). | Lienzo en blanco con foco directo en título si es nota nueva. | Hoja oculta o estado inactivo en memoria. |
| **2** | **Loading (Skeleton)** | Skeletons de 3 tarjetas de tarea y 2 chips de notas. | Bloqueo leve con spinner lineal superior en guardado inicial. | Indicador circular pulsante + texto: *"Analizando nota en local..."*. |
| **3** | **Success (Content)** | Lista ordenada de tareas de hoy + notas recientes con interacciones activas. | Texto formateado con resaltado de sintaxis Markdown y chips contextuales. | Lista de tareas detectadas con checkboxes y fechas sugeridas. |
| **4** | **Partial / Draft** | Tarea creada localmente pero pendiente de persistencia atómica en Room. | Borrador preservado en `SavedStateHandle` tras rotación o interrupción. | Extracción con 2 tareas válidas y 1 advertencia de texto ambiguo. |
| **5** | **Empty State** | Ilustración plana + texto: *"Día libre de pendientes"* + Botón de captura rápida. | Estado vacío: *"Escribe tu primera idea"* con botón de plantilla. | Mensaje informativo: *"No se encontraron tareas pendientes en esta nota"*. |
| **6** | **Error (Hard Failure)** | Banner superior no intrusivo con botón de reintentar lectura de base de datos. | Aviso flotante: *"Error al guardar nota"* preservando el texto en buffer. | Alerta: *"No fue posible procesar el texto"* + botón de reintento. |
| **7** | **Offline / Degraded** | Operación 100% transparente (Room actúa como Single Source of Truth). | Operación 100% local sin indicadores de advertencia. | Banner: *"LiteRT no descargado. Conéctate para procesar con IA Cloud"*. |
| **8** | **Stale / Syncing** | Chip discreto *"Actualizando..."* en la barra superior mientras Room refresca. | Indicador de timestamp sutil: *"Guardado a las 14:05"*. | N/A (operación transaccional de un solo uso). |
| **9** | **Empty Filter / Search** | *"No hay tareas pendientes para este criterio"* + acción para limpiar filtro. | *"Sin notas que coincidan con la búsqueda"*. | N/A. |
| **10** | **Permission Required** | Badge sutil invitando a activar notificaciones si se ignoró el permiso. | N/A (el editor no requiere permisos del sistema). | N/A. |
| **11** | **Active Action / Processing** | Tarea tachada transitoria + Snackbar de "Deshacer" (ventana de 4 segundos). | Chip flotante *"Generando tarea..."* al resaltar un bloque de texto. | Botón *"Agregar (N) tareas"* deshabilitado mientras se insertan en Room. |
