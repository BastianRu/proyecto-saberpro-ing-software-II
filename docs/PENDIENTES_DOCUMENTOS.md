# Pendientes de documentación

> Inventario generado en 2026-11-22. Responsable indicado por tipo de evidencia; todos los elementos aún sin respaldo se mantienen como pendientes.

## Equipo

- Prototipos y evaluación formal de usabilidad de la iteración 2 no encontrados en el repositorio. El SAD describe esta ausencia sin marca de completar; adjuntar evidencia solo si forma parte del paquete de entrega.
- Product Backlog, Sprint Backlog, Task Board y Burndown Chart se entregan mediante el acceso de Jira/Trello; no se generan dentro del SAD.
- El enlace o evidencia audiovisual de sustentación no forma parte de la solicitud documental indicada; entregar por el medio definido por el curso si se requiere.
- La revisión del Pull Request, la aceptación del Product Owner y el entorno de demostración corresponden a evidencias del proceso; no son necesarias para entregar el PDF del SAD solicitado.
- Pendientes técnicos en `docs/PENDIENTES.md`: acordar Outbox, recuperación de eventos y DLQ; consistencia entre Revisión y Banco; migración del cliente; gestión del secreto; persistencia del broker; estrategia de escalabilidad y CI/cobertura.
- MiniQAW v2: validar todas las estimaciones numéricas y duraciones propuestas.

## Validaciones de contenido

- Las cifras del MiniQAW v2 son propuestas de esfuerzo humano; acordarlas con el equipo y el profesor antes de presentarlas como compromiso.
- La lista de patrones que el curso considera «vistos» requiere validación del profesor.
- La aceptación académica de SQLite y la evaluación de escalabilidad dependen del criterio del curso.
- El alcance de HU05 se describe como parcial en el SAD y debe presentarse con esa salvedad.

## Jira

- Product Backlog, Sprint Backlog, Task Board y Burndown Chart como evidencia del Sprint.
- Evidencia de aceptación del Product Owner y PR revisado.

## Restricción de compilación

- Los PDFs LaTeX se generaron con Tectonic por estar ausente `pdflatex`/TeX Live y no existir permisos `sudo` para instalarlo. **[POR COMPLETAR: ejecutar dos compilaciones con `pdflatex` en un entorno con TeX Live y confirmar cero errores.]** Responsable: equipo.
