# Pendientes de arquitectura y operación

> Actualizado el 2026-11-22 tras verificar código de los cuatro servicios. Las decisiones que ya están implementadas se retiraron de este listado; las dudas que requieren acuerdo del equipo o del profesor continúan marcadas.

## Resueltos en el código

- Existe `revision-service` (puerto 8083), con base propia, consumidor de `question.state-changed` y flujo parcial de revisión por pares. Reemplaza el servicio de validación independiente sugerido inicialmente como cuarto servicio.
- La validación estructural está embebida como Pipeline de siete filtros en `banco-service`; no hay módulo de producción `validacion-service`.
- `auditoria-service` consume eventos de creación y cambio de estado; Revisión consume eventos de cambio de estado. Se configuran colas durables separadas.

## Pendientes del equipo

1. **Interfaz de escritorio:** migrar las operaciones del Banco desde SQLite local al servicio Banco y evaluar integración de revisión. Actualmente solo el inicio de sesión llama a `auth-service` (`src/main/java/com/unicauca/lisw2t2g08/main/MainApp.java`).
2. **Entrega fiable de eventos:** el Banco puede guardar la pregunta y perder el evento si RabbitMQ falla; no existe Outbox. Evaluar Outbox y recuperación/reconciliación.
3. **Mensajes fallidos:** Revisión y Auditoría reintentan hasta 3 veces y luego descartan sin DLQ (`revision-service/src/main/resources/application.properties`, `auditoria-service/src/main/resources/application.properties`). Definir retención, alerta y recuperación de eventos fallidos.
4. **Persistencia del broker:** Compose no define volumen para datos de RabbitMQ; una cola durable por sí sola no demuestra retención ante reinicio/pérdida del contenedor. Definir persistencia de despliegue y verificarla.
4. **Consistencia del flujo de revisión:** si Banco acepta el cambio y falla la persistencia posterior en `revision.db`, ambos servicios pueden quedar en estados distintos. Definir recuperación/reconciliación.
5. **SQLite y escalabilidad:** las conexiones compartidas y métodos `synchronized` no acreditan despliegue de varias instancias. Definir persistencia operable bajo concurrencia o aceptar/documentar el límite académico.
6. **Gestión del secreto:** los cuatro servicios comparten un secreto por defecto de desarrollo. Definir inyección segura, rotación y protección en cualquier demostración no local.
7. **Alcance HU05:** quedan por definir o fuera de alcance asignación automática de revisores y rúbrica/formato completo; ver `docs/decisiones-iteracion-2.md`.
8. **Esquema temporal del token:** el formato de vencimiento ISO-8601 compartido debe seguir siendo interoperable. Acordar el tratamiento de zonas horarias antes de distribuir servicios en entornos heterogéneos.
9. **CI y cobertura:** no se encontró `.github/workflows/` ni configuración de cobertura JaCoCo. Definir automatización y umbral si el curso lo exige.
10. **Script E2E:** `scripts/e2e.sh` llama al servicio eliminado y debe actualizarse antes de usarse. No se modifica aquí por ser script ejecutable.
11. **Documentación de usuario y prototipos:** confirmar material de usuario y prototipos, que no pueden inferirse del código.

## Pendientes del profesor

- Confirmar cuáles son los «patrones vistos» requeridos para el criterio académico.
- Validar si la arquitectura con SQLite cumple las expectativas del enunciado sobre escalabilidad y motor de base de datos.
- Validar el alcance de HU05 y las estimaciones de esfuerzo propuestas en el MiniQAW.

## Pendientes de Jira

- Adjuntar Product Backlog, Sprint Backlog, Task Board y Burndown Chart; son evidencias del gestor de tareas, no artefactos inferibles desde el repositorio.
- Adjuntar revisión/aprobación de Pull Request, aceptación del Product Owner y captura del entorno de demostración.

