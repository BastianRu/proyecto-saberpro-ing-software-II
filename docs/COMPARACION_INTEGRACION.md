# Histórico: Comparación e integración de las dos versiones

> **Histórico — registro de integración del 2026-11-20; no es guía de ejecución vigente.** Se conserva la comparación y sus decisiones como trazabilidad. Para el estado actual consulte `README.md`, los README de los servicios y `docs/decisiones-iteracion-2.md`.
>
> Comparación realizada entre la rama local `integracion-iteracion2` (antes de integrar, `522fad5`) y la versión actualizada de `origin/main` (`97d3233`). La integración quedó en `c227983`. Se mantuvo una referencia de respaldo `backup-integracion-iteracion2` al estado anterior.

## Resumen de decisión

Se integró `origin/main` como base porque representa la iteración más reciente y amplía el alcance del sistema con la revisión por pares y la auditoría de eventos. Se conservaron además las correcciones y salvaguardas de la rama local que siguen siendo compatibles con esa arquitectura. La rama remota `main` no se modificó ni se hizo push.

## Funcionalidades compartidas y comparación

| Área | Aporte de la rama local (`522fad5`) | Aporte de `origin/main` (`97d3233`) | Evaluación e integración |
|---|---|---|---|
| Autenticación y usuarios | `auth-service` integrado, cliente JavaFX conectado al login, pruebas y documentación del servicio. | Conserva `auth-service` y corrige la emisión de vencimiento para usar `Clock.systemUTC()`. | Se conserva la arquitectura y el cliente de la rama local; se adopta UTC de la versión remota. Se restauró la prueba de vector fijo del token en emisor y verificador del Banco. |
| Banco de preguntas | Servicio Banco separado de validación; cliente REST a `validacion-service`; respuesta `503` cuando validación no está disponible; contrato HTTP y Postman por servicio; prueba E2E. | Embebe el pipeline en `banco-service`, formaliza el ciclo con clases del patrón State y mejora separación de DTOs, eventos y casos de uso. | Se adopta el Banco actualizado y su pipeline local, coherente con el diseño de la iteración. La prueba y cliente REST de validación se retiran porque ya no existe ese servicio. Se preserva el aprendizaje de manejo de indisponibilidad como limitación/documentación, no como llamada obsoleta. |
| Máquina de estados | Enum y transiciones en Banco; historial guardado allí. | Jerarquía polimórfica de estados, doble despacho y estados compartidos; auditoría externa como proyección de eventos. | La versión remota es mejor para demostrar el patrón State y centralizar cada regla con su estado. Auditoría es eventualmente consistente, no reemplaza una transacción local. |
| Eventos | Publicación de creación, edición y cambios de estado; `QUESTION_UPDATED`; sin consumidores. | Contratos tipados `QuestionCreated` y `QuestionStateChanged`, `eventId`, exchange topic y colas para Revisión y Auditoría. | Se adopta el contrato tipado y los dos consumidores. No se conserva `QUESTION_UPDATED`, que la versión remota no publica ni consume. |
| Auditoría | Historial persistido en la base del Banco y endpoint de historial del propio Banco. | `auditoria-service` persiste eventos con `event_id UNIQUE` y expone historial cronológico separado. | Se adopta el servicio independiente y su deduplicación. No ofrece transacción atómica con Banco: falta patrón Outbox y un evento puede perderse. |
| Revisión | Rol `REVISOR` y cambio de estado manual en Banco, pero sin flujo de revisión de pares. | `revision-service` escucha eventos, crea una revisión, llama al Banco con el token del revisor y registra dictamen/observaciones. | La versión remota cubre más flujo de negocio. Quedan fuera asignación automática y rúbrica completa según sus propias decisiones de alcance. |
| Pruebas y guías | E2E en Bash, guía de ejecución, reglas y notas de riesgos para la versión anterior. | Suites unitarias por servicio, pruebas de revisión/auditoría, Postman unificado, Docker Compose y decisiones de integración. | Se conservan los documentos de origen solo si siguen siendo correctos; los scripts/documentos que nombran `validacion-service` o el flujo antiguo no deben presentarse como guía vigente. |
| Monolito JavaFX | Iteración 1 y login conectado a auth-service; el resto aún usa banco local. | Mantiene el monolito y explica el alcance limitado del cliente. | Se conserva como legado y se documenta explícitamente la deuda de integración del cliente con `banco-service`. |

## Qué versión es mejor y qué le faltaba a cada una

### Versión actualizada de `origin/main`

**Mejoras:** cubre más negocio (revisión y auditoría), elimina un salto REST síncrono por creación/edición al embutir el pipeline, utiliza clases de estado, identifica eventos con UUID, deduplica auditoría y prepara RabbitMQ con Docker Compose.

**Faltantes o riesgos que siguen:** entrega de eventos no atómica (sin Outbox), consistencia entre la transición en Banco y la escritura local de Revisión, autorización detallada de algunas consultas, alcance parcial de HU05, SQLite con límites de escalado y cliente JavaFX que todavía no consume el Banco remoto. Además, la versión publicada removió algunas colecciones Postman específicas y la guía E2E previa no era compatible con los nuevos endpoints/eventos.

### Rama local `integracion-iteracion2`

**Mejoras:** tenía un contrato HTTP más explícito para `validacion-service`, respuesta 503 ante caída del validador, matriz y documentación detalladas, vector de contrato del token, prueba E2E y registro de pendientes. Estos aportes eran útiles para la versión anterior.

**Faltantes:** no tenía consumidores de RabbitMQ, revisión por pares real ni auditoría como servicio; mantenía la validación como dependencia síncrona; carecía del patrón State del Banco actualizado y presentaba informes de estado/guías que quedaron desactualizados al cambiar la arquitectura.

## Combinación aplicada

- Avance local de `main` a `origin/main` y merge de esa base en `integracion-iteracion2`.
- Se mantuvieron íntegros `auth-service`, los cambios del cliente JavaFX y los componentes del monolito de `src/` que `origin/main` no eliminó.
- Se adoptaron `revision-service`, `auditoria-service`, `docker-compose.yml`, la colección Postman unificada, el Banco con pipeline embebido y el State polimórfico.
- Se resolvió el conflicto del reloj eligiendo UTC en `auth-service`.
- Se restauraron pruebas de vector fijo de token en `auth-service` y `banco-service`, adaptando el verificador al contrato de texto/rol usado en la rama actual.
- Se retiraron pruebas y código cliente de `validacion-service` que ya no existe en el diseño resultante.
- Se preservó `docs_2/` como contenido no rastreado aportado localmente y se rehízo `SAD.docx` con la plantilla `SAD.docx`; se generó también `SAD.pdf` para revisión visual.

## Verificación después de integrar

Ejecutado `mvn test` de forma separada en cada módulo. Resultados obtenidos en el entorno:

| Módulo | Pruebas | Fallos | Errores | Resultado |
|---|---:|---:|---:|---|
| Proyecto raíz (JavaFX / monolito legado) | 158 | 0 | 0 | Pasa |
| `auth-service` | 21 | 0 | 0 | Pasa |
| `banco-service` | 100 | 0 | 0 | Pasa |
| `revision-service` | 40 | 0 | 0 | Pasa |
| `auditoria-service` | 9 | 0 | 0 | Pasa |
| **Total de ejecuciones de pruebas** | **328** | **0** | **0** | **Pasa** |

El total cuenta casos ejecutados por Maven en cada módulo y no necesariamente equivale a casos únicos si existen pruebas compartidas o parametrizadas.

## Tareas recomendadas

1. Actualizar o reemplazar la antigua prueba `scripts/e2e.sh` antes de usarla: apunta a `validacion-service`, al endpoint de historial del Banco y a claves de eventos de la versión anterior.
2. Implementar Outbox transaccional para no perder eventos publicados después de persistir una pregunta.
3. Definir idempotencia y recuperación en Revisión cuando el Banco completa una transición pero falla la escritura de `revision.db`.
4. Completar la conexión del cliente JavaFX con los endpoints REST de Banco y Revisión, o declararlo explícitamente fuera del alcance de esta entrega.
5. Confirmar con el equipo/profesor la aceptación de SQLite frente al requisito académico de base de datos y documentar la decisión real.
6. Inyectar secretos por variables de entorno en despliegues no locales y asegurar UTC homogéneo en todos los consumidores del token.
7. Confirmar si las consultas abiertas en Banco y Auditoría son intencionales; la autenticación presente no implica autorización por rol en esas rutas.
