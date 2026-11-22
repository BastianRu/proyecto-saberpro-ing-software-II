# Información redundante entre servicios

> **Verificado contra el código actual el 2026-11-22.** La repetición de contratos es deliberada cuando permite que cada servicio conserve su independencia; debe gestionarse mediante compatibilidad documentada y pruebas, no mediante acoplamiento de código compartido.

## Criterio

Los servicios son módulos Maven independientes. No comparten una librería Java de dominio; algunos valores y formatos deben coincidir porque viajan por token, JSON o eventos. `src/` representa el monolito de la primera iteración y no es dependencia de los servicios.

## Contratos repetidos verificados

| Elemento | Dónde se repite | Motivo | Riesgo y criterio de mantenimiento |
|---|---|---|---|
| Roles (`ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE`, `ESTUDIANTE`) | `auth-service/.../auth/domain/Role.java`; `banco-service/.../banco/domain/Role.java`; `revision-service/.../revision/domain/Role.java`; `auditoria-service/.../auditoria/domain/Role.java`; monolito `src/.../domain/Role.java`. | Auth emite el rol; los servicios consumidores del token autorizan operaciones localmente; el monolito preserva la iteración anterior. | Contrato sensible: cambiar un valor requiere coordinar emisor, verificadores, autorización, pruebas y documentación. No importar clases de un servicio a otro. |
| Token (`subject`, rol, vencimiento y firma HMAC-SHA256) | Emisor `auth-service/.../auth/service/TokenSigner.java`; verificadores `*/security/TokenVerifier.java` en Banco, Revisión y Auditoría; formatos auxiliares `AuthToken` locales. | Verificación autónoma evita solicitar a Auth en cada llamada. | Los verificadores deben mantener idénticos parsing, codificación, reloj y secreto. Hay pruebas de vector fijo en Auth y Banco; ampliar pruebas de contrato para Revisión y Auditoría es una mejora sugerida, no un hecho actual. |
| Secreto del token | `auth-service/src/main/resources/application.properties` (`auth.token.secret`) y las propiedades `auth.token.secret` de Banco, Revisión y Auditoría. | El emisor firma y cada servicio valida localmente. | En el repositorio hay un valor de desarrollo duplicado. En despliegue no local debe inyectarse y rotarse coordinadamente; no registrar el valor en documentación externa. |
| Códigos de estado de pregunta | Banco (`domain/*State.java` y `QuestionStates.java`), Revisión/Auditoría como texto en mensajes y registros, y cliente JavaFX en `src/`. | El Banco es dueño del ciclo de vida; consumidores necesitan proyectar estados recibidos, sin acceder a su base. | Cambios en nombres/códigos requieren compatibilidad hacia adelante/atrás en consumidores. Las referencias `question_id` no son llaves foráneas entre bases. |
| Contrato de eventos | Banco: `events/QuestionCreatedEvent.java`, `QuestionStateChangedEvent.java`; Revisión: `events/QuestionStateMessage.java`; Auditoría: `events/QuestionEventMessage.java`. | Cada servicio define DTO local para sus responsabilidades, sin dependencia binaria. | Mantener nombres/semántica de `eventId`, `questionId`, `username`, `occurredAt`, estados y claves de routing `question.created`, `question.state-changed`. `revision-service` consume solo el segundo tipo; Auditoría consume ambas claves. |
| Contrato de petición al Banco | `revision-service/.../service/RestBancoClient.java` y Banco `api/QuestionController.java` (`PATCH /preguntas/{id}/estado`). | Revisión delega al dueño del ciclo de vida; pasa token del revisor para conservar identidad y autorización. | El endpoint, payload y respuestas deben evolucionar de forma coordinada. No se comparte el DTO Java. |
| Entidad `Question` / pregunta | Banco contiene la entidad completa; Revisión guarda `question_id` como texto en `reviews`; Auditoría guarda `question_id` en `audit_records`; JavaFX conserva un modelo local separado. | Los servicios de revisión y auditoría referencian la identidad sin ser dueños de los datos de la pregunta. | Las referencias cruzadas no son FK. Borrados físicos están ausentes en Banco; los estados y relaciones se validan vía eventos/API. Evitar replicar detalles de la entidad sin necesidad de negocio. |
| Nivel de dificultad | Banco y monolito tienen tipos locales; el valor se transporta como parte de solicitudes de pregunta. | Cada módulo compila sin depender de otro. | Mantener códigos `BAJA`, `MEDIA`, `ALTA` compatibles con API y almacenamiento. |

## Lo que no debe confundirse con duplicación accidental

- `question_id` en `revision.db` y `auditoria.db` es referencia por identificador, no una copia de la fila `questions`; no existe llave foránea entre SQLite independientes.
- El historial canónico expuesto en iteración 2 se conserva en Auditoría como proyección de eventos; el Banco persiste su propia pregunta y no importa datos de Auditoría.
- Los DTO de eventos son copias locales; compartirlos en un JAR común acoplaría despliegues y versiones.
- El patrón Observer de `src/` opera dentro de la GUI local y no sustituye a RabbitMQ.

## Reglas de cambio recomendadas

1. Todo cambio de contrato debe citarse en un ADR o decisión y actualizar productores, consumidores y pruebas afectadas en el mismo cambio coordinado.
2. Mantener compatibilidad de nombres de eventos y campos; validar con `EventContractTest` en Auditoría y pruebas de contrato de Revisión.
3. No crear una dependencia de código entre servicios para eliminar una repetición pequeña; priorizar contratos estables y pruebas.
4. Toda nueva referencia entre servicios debe identificarse como referencia lógica, sin aparentar una FK que SQLite no puede hacer cumplir entre archivos.

**Fuentes:** `docs/decisiones-iteracion-2.md`; clases citadas arriba; `banco-service/src/main/java/.../config/BancoConfig.java`, `revision-service/src/main/java/.../config/ReviewConfig.java` y `auditoria-service/src/main/java/.../config/AuditConfig.java`.
