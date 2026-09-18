# Riesgos vigentes

Revisado: 2026-09-17. La mitigación se implementa únicamente mediante una spec y pruebas trazables.

| ID | Riesgo | Mitigación / evidencia requerida |
|---|---|---|
| R-01 | IA local no disponible, lenta o costosa | Captura manual siempre disponible; resultado tipado; medir latencia/memoria antes de ampliar motores. ADR-019. |
| R-02 | Sugerencias incorrectas crean trabajo no deseado | Previsualización y confirmación explícita; probar que cancelar no escribe en Room. |
| R-03 | Pérdida de borradores por muerte del proceso | Persistencia durable y recuperación de ADR-013; verificar con pruebas de repositorio y recreación. |
| R-04 | Expansión dispersa debilita el núcleo | Mantener Proyectos/Hábitos/Focus activos, priorizar Tareas/Notas/IA; una vertical slice y gates completos por cambio. |
| R-05 | Limpieza de módulos rompe rutas o datos existentes | Inventario de referencias, migraciones preservadas, pruebas de navegación y Room antes de retirar archivos. |
| R-06 | Contratos históricos se confunden con funcionalidades reales | Estados `Vigente`, `Propuesto`, `No verificado` en specs y evidencia junto a cada requisito. |
