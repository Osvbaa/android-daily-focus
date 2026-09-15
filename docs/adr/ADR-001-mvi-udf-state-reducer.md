ADR-001: MVI / UDF con Reducer Funcional para Estados Complejos
Estado: Aceptado

Fecha: 2026-09-07

Módulos afectados: :feature:*, :core:ui, :core:designsystem

Contexto
El desarrollo en Jetpack Compose requiere un flujo unidireccional de datos (UDF). Un enfoque tradicional de MVVM con múltiples MutableStateFlow independientes provoca desincronización de UI, condiciones de carrera (state tearing) y dificulta el testing de transiciones complejas (como el temporizador de Pomodoro fluctuando entre Running, Paused y Overtime, o la ventana de 4 segundos de Undo en Tareas). Asimismo, los agentes de IA tienden a crear estados mutables dispersos si no se impone un contrato rígido.

Decisión
Contrato UDF Estricto: Todo ViewModel de pantalla expone exclusivamente:

Un único StateFlow<UiState> inmutable.

Una función onEvent(event: UiEvent) para recibir intenciones de usuario.

Un canal no persistente Channel<UiEffect>(Channel.BUFFERED) expuesto como Flow<UiEffect> para eventos de un solo disparo (navegación, snackbars hápticos).

Inmutabilidad Absoluta: Todas las colecciones dentro de cualquier UiState deben ser ImmutableList<T>, ImmutableSet<T> o ImmutableMap<T, K> provenientes de kotlinx.collections.immutable. Prohibido el uso de colecciones estándar de Java/Kotlin en el estado.

Máquinas de Estado Complejas: Para módulos con transiciones de concurrencia sensible (Pomodoro, Extracción de IA), el cálculo del nuevo estado se delega a una función pura Reducer: (PreviousState, UiEvent) -> NextState.

Consecuencias
Positivas: Reproducibilidad determinista de bugs en UI; compatibilidad nativa con Compose Recomposition Optimization; cero inconsistencias de estado.

Negativas: Incremento de boilerplate al declarar clases selladas para cada pantalla.

Verificación: Regla de Konsist en :core:testing que verifica que ningún ViewModel exponga propiedades de tipo MutableStateFlow públicas y que todo UiState resida en un archivo sellado.