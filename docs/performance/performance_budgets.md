Presupuestos de Rendimiento (Performance Budgets)
1. Presupuestos Cuantitativos No Negociables
   Todo build de release (app-release.aab) o PR que sobrepase estos umbrales fallará automáticamente el pipeline de CI:

2. Métrica,Objetivo (P50),Umbral de Fallo (P90),Herramienta de Medición
   Cold Start (Time to Initial Display - TTID),<600 ms,>900 ms,Macrobenchmark / Android Vitals
   Cold Start (Time to Full Display - TTFD),<1000 ms,>1400 ms,reportFullyDrawn() + Macrobenchmark
   Warm Start,<250 ms,>400 ms,Macrobenchmark
   Hot Start,<120 ms,>200 ms,Macrobenchmark
   Jank / Frozen Frames (UI Thread),<1.0%,>2.5% de frames jank,JankStats API
   Frame Render Time,120 Hz (<8.3 ms),>16.6 ms (caída a <60 Hz),Perfetto Trace
   Tamaño de Descarga (AAB / Play Store),<12.0 MB,>18.0 MB (sin pesos IA),Diffuse / Bundletool
   Consumo de Memoria RAM (En reposo),<85 MB,>130 MB,Android Profiler
   Consumo de Memoria RAM (Extracción IA),<220 MB,>350 MB,Android Profiler

3. Aclaración sobre el tamaño: El tamaño del AAB base comprende exclusivamente el código compilado, recursos vectoriales y motor de inferencia LiteRT sin pesos preinstalados. Los pesos del modelo SLM se descargan bajo demanda mediante Play Asset Delivery (PAD) o Ktor background sync.2. Protocolo de Inicio Rápido (Cold Start Optimization)Para sostener un TTID $< 600\text{ ms}$, la inicialización de la aplicación sigue una política de Cero Bloqueo de Hilo Principal:Application.onCreate() Estricto:Prohibido realizar llamadas I/O a disco, inicializaciones síncronas de Room, lecturas de DataStore o peticiones de red en el hilo principal.La inyección de dependencias con Hilt debe priorizar proveedores de inicialización diferida (Lazy<T>) para repositorios y dependencias pesadas.Medición de reportFullyDrawn():El composable raíz de DashboardScreen debe invocar ReportDrawnWhen de Compose Runtime únicamente cuando los flujos iniciales de tasks y notes hayan emitido su primer estado Success desde Room:KotlinReportDrawnWhen {
   uiState is TasksUiState.Success && notesUiState is NotesUiState.Success
   }
4. Presupuesto de Frames y Renderizado en ComposeRecomposiciones Innecesarias:Toda clase de datos pasada como parámetro a un Composable debe ser evaluada como Stable o Immutable por el compilador de Compose.Prohibido pasar lambdas inestables sin memorizar (remember) dentro de ítems de LazyColumn.Instrumentación con JankStats:El módulo :core:ui registrará un listener global de JankStats durante builds de pre-producción para tracear las caídas de cuadros en listas extensas:Kotlinval jankListener = JankStats.OnFrameListener { frameData ->
   if (frameData.isJank) {
   Log.w("Performance", "Jank detectado en: ${frameData.states}")
   }
   }
5. Estrategia de Baseline ProfilesEl repositorio incluirá un módulo dedicado :benchmarks:macrobenchmark para compilar perfiles de optimización AOT (Ahead-of-Time).Recorridos Críticos a Perfilar:Arranque en frío directo a "Mi Día": Precompilación de clases de Compose, Room y Dispatchers.Scroll en LazyColumn de tareas: Optimización del reciclaje de nodos de Compose.Apertura de la hoja inferior de IA: Compilación previa de layouts complejos antes del primer tap del usuario.Comando de Generación:Bash./gradlew :benchmarks:macrobenchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
   Los perfiles generados (baseline-prof.txt) deben residir en :app/src/main/baselineProfiles/ y versionarse en Git.5. Presupuesto de Memoria y Prevención de Fugas (Leaks)Alineación de Páginas de 16 KB:Ninguna librería C/C++ vinculada (como el motor de SQLite o LiteRT) puede violar la alineación ELF de 16 KB.El script de CI scripts/ci/check_elf_alignment.py validará el binario en cada release build.Cero Retención de Contexto:Prohibido almacenar referencias a Context dentro de singletons, repositorios o ViewModels.Uso obligatorio de ApplicationContext cuando sea requerido por APIs del sistema.Cancelación de Corrutinas:Todo trabajo diferido en segundo plano debe ligarse al viewModelScope o a un CoroutineScope supervisado con cancelación estructurada, impidiendo jobs huérfanos al salir de una pantalla.
