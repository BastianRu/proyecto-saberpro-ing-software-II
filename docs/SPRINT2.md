# Sprint 2 — objetivo y Definition of Done

> **Estado:** borrador documental verificado contra `main` el 2026-11-22. Aprobación, aceptación del Product Owner y evidencias de Jira no se pueden inferir del código.

## Sprint Goal (borrador)

Como equipo, buscamos que la gestión de preguntas pueda operar mediante servicios separados y que los cambios de estado alimenten una revisión por pares y un historial consultable, manteniendo el acceso de usuarios por rol. La entrega logra esta meta en los servicios, aunque la interfaz de escritorio conserva localmente el manejo del banco.

## Definition of Done

| Ítem | Estado verificable | Evidencia |
|---|---|---|
| Código integrado en `main` mediante Pull Request revisado | **No verificable** | Git conserva commits de integración, pero no hay evidencia de URL, revisión ni aprobación de Pull Request en el repositorio. Requiere Jira/GitHub. |
| Pruebas unitarias escritas y pasando | **Verificado: Sí** | `mvn test` ejecutado el 2026-11-22: Auth 21, Banco 94, Revisión 40, Auditoría 9; todos `BUILD SUCCESS`, cero fallos/errores. También raíz 158. |
| Cobertura acordada (ej. >80% lógica de negocio) | **No verificable** | No se encontró configuración JaCoCo ni reporte de cobertura; cantidad de pruebas no equivale a porcentaje de cobertura. |
| Integración continua en verde | **No verificable / sin configuración encontrada** | No existe `.github/workflows/` en el repositorio. |
| Refactorización completada, sin problemas críticos nuevos | **Parcial** | Cuatro servicios existen; cliente JavaFX solo usa Auth y mantiene Banco local (`src/main/java/com/unicauca/lisw2t2g08/main/MainApp.java`). No hay evidencia de revisión de defectos críticos o aceptación formal. |
| Si impacta arquitectura: SAD actualizado (C4 del componente) y ADR registrado | **Parcial en la rama revisada** | El SAD oficial entregable está en `entregables/sprint-2/SAD_Banco_Preguntas_Iteracion2.pdf` (fuente: `docs_2/SAD.pdf`); contiene C4 niveles 1–3 y decisiones D-001 a D-008. Esta rama añade ADR individuales y diagramas de apoyo, sin sustituir ese SAD. No se acredita que los artefactos estén integrados en `main`/PR. |
| Desplegado en entorno de demostración | **No verificable** | Hay instrucciones locales en `docs/decisiones-iteracion-2.md`; no se encontró URL, evidencia de entorno compartido ni captura de despliegue. |
| Aceptado por el Product Owner | **No verificable** | Requiere registro de Jira o acta; no se inventa. |
| Documentación mínima de usuario actualizada | **Parcial** | README y guías técnicas existen, pero no se encontró manual de usuario final ni ayuda de la interfaz para operaciones distribuidas. |

## Evidencias pendientes de Jira

- **Product Backlog:** **[POR COMPLETAR: Jira — adjuntar exportación o captura del Product Backlog.]**
- **Sprint Backlog:** **[POR COMPLETAR: Jira — adjuntar exportación o captura del Sprint Backlog.]**
- **Task Board:** **[POR COMPLETAR: Jira — adjuntar captura del tablero.]**
- **Burndown Chart:** **[POR COMPLETAR: Jira — adjuntar gráfica del Sprint 2.]**
- Revisión y aprobación del Pull Request: **[POR COMPLETAR: equipo — agregar enlace y evidencia.]**
- Aceptación del Product Owner: **[POR COMPLETAR: equipo/PO — registrar resultado.]**
- Despliegue de demostración: **[POR COMPLETAR: equipo — aportar URL y evidencia.]**
