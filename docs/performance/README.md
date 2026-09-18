# Rendimiento: contrato de medición

Estado: presupuestos **provisionales, no gates de CI**. Los números del [documento histórico](performance_budgets.md) no tienen aún dispositivo de referencia, baseline, macrobenchmark ni tareas CI que permitan afirmar cumplimiento o fallo automático. No deben citarse como resultados.

Antes de fijar un umbral obligatorio, definir dispositivo/OS, build release, escenario, número de muestras, percentil, herramienta y propietario de regresiones. Medir arranque frío y tiempo hasta contenido útil, latencia de captura, frames lentos en listas, RAM en reposo e inferencia, tamaño instalado/AAB y alineación de binarios nativos de 16 KB. Registrar fecha y artefacto de medición.

Restricciones ya aplicables: no registrar contenido de notas/tareas ni PII en logs; no bloquear hilo principal con I/O; respetar límites de background y cancelación de corrutinas. La cobertura debe tener pruebas o inspección verificable cuando se cambie código relacionado.
