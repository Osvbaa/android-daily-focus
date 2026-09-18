# Plan: integridad de proyectos

Estado: lógica y persistencia verificadas; UI visual pendiente del propietario. Requisitos PROJECT-02, PROJECT-03 y PROJECT-04.

Domain: normalizar títulos antes de validar unicidad. Data: conservar la transacción de creación `ProjectDao.createProjectWithMilestones` y el archivado que solo cambia `isArchived`; verificar la validación ya existente de pertenencia de hito al guardar tareas. ViewModel: no requiere nuevo evento para estas reglas. UI: comunicar fallo de validación por el `errorMessage` existente; ver [contrato](ui-contract.md). Tests: integración Room para duplicados normalizados, preservación tras archivar y rechazo de hito ajeno. Sin cambio de esquema ni migración.
