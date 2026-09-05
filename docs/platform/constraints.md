# Platform Constraints & Baseline (2026)

## Target Platform Specifications
* **Min SDK**: 26 (Android 8.0 Oreo) — Asegura soporte para APIs modernas de java.time y cifrado básico.
* **Target SDK**: 37 (Android 17) — Requisito obligatorio para publicación de apps nuevas en Google Play.
* **Compile SDK**: 37

## Mandatory System Behaviors (Enforced by OS)
1. **Edge-to-Edge**: No desactivable en Target SDK 36. Toda superficie debe manejar `WindowInsets` seguros.
2. **Predictive Back Gestures**: Mandatorio para animaciones de salida y retorno de pantallas.
3. **16 KB Memory Page Alignment**: Todo binario nativo (.so) empaquetado debe estar alineado a páginas de 16 KB (`useLegacyPackaging = false` cuando se configure el build system).
4. **Background Execution**: Restricciones de Doze Mode y ejecución en segundo plano para tareas prolongadas (requiere cálculo con `SystemClock.elapsedRealtime()` para temporizadores).