# Reglas del Banco de Preguntas Saber Pro

> **Fuente:** código de los servicios y del cliente en la rama `main`, verificado el 2026-11-22. Cuando difiera un documento anterior, prevalece el código. Las rutas mostradas son relativas a la raíz del repositorio.

## Usuarios y token

| Código | Regla comprobable | Dónde se aplica |
|---|---|---|
| U-01 | Los roles admitidos son `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE`, `ESTUDIANTE`. | `auth-service/src/main/java/com/unicauca/lisw2t2g08/auth/domain/Role.java`; duplicados locales en servicios consumidores. |
| U-02 | El login es único; el repositorio persiste contraseñas hasheadas, nunca el valor recibido. | `auth-service/src/main/java/com/unicauca/lisw2t2g08/auth/config/AuthConfig.java`; `auth-service/src/main/java/com/unicauca/lisw2t2g08/auth/access/SQLiteUserRepository.java`; `Argon2PasswordHasher.java`. |
| U-03 | La contraseña requiere al menos 6 caracteres, un dígito, una mayúscula y un carácter especial. | `auth-service/src/main/java/com/unicauca/lisw2t2g08/auth/domain/PasswordPolicy.java`. |
| U-04 | El token es `base64url(login|ROL|vencimiento).base64url(HMAC-SHA256)`; se verifica localmente en Banco, Revisión y Auditoría con secreto compartido. | `auth-service/.../service/TokenSigner.java`; `*/security/TokenVerifier.java`; propiedades `auth.token.secret`. |
| U-05 | El token debe llegar como `Authorization: Bearer <token>` y debe ser válido/no expirado. | `banco-service/.../security/BearerAuthFilter.java`; filtros equivalentes de Revisión y Auditoría. |
| U-06 | Login fallido responde 401 sin distinguir usuario inexistente, inactivo o contraseña errónea; alta duplicada responde 409. | `auth-service/.../service/UserService.java`; `auth-service/.../api/ApiExceptionHandler.java`. |

## Preguntas

| Código | Regla comprobable | Dónde se aplica |
|---|---|---|
| P-01 | El identificador de pregunta es `P-###`; el Banco asigna el siguiente identificador según preguntas existentes. | `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/service/QuestionService.java`. |
| P-02 | Una pregunta nueva se guarda en `BORRADOR`; no existe operación de borrado físico. | `banco-service/.../service/QuestionService.java`; `banco-service/.../access/IQuestionRepository.java`. |
| P-03 | Solo se edita contenido en estado `BORRADOR`. | `banco-service/.../domain/Question.java`; `banco-service/.../domain/BorradorState.java`. |
| P-04 | La búsqueda admite texto, estado, dificultad, competencia, tema y subtema. | `banco-service/.../api/QuestionController.java`; `banco-service/.../domain/QuestionCriteria.java`. |
| P-05 | La pregunta mantiene título, contexto, pregunta directa, opciones, índice correcto, justificación, bibliografía, competencia, tema, subtema y dificultad. | `banco-service/.../domain/Question.java`, `QuestionDraft.java`; `banco-service/.../config/BancoConfig.java`. |

## Validación estructural: Pipeline de siete filtros

Se ejecuta en orden y está embebido en `banco-service`; no hay servicio independiente de validación.

| Orden / código | Regla | Clase que la aplica |
|---|---|---|
| 1 — RF-08 | Contexto presente y, tras recortar espacios, al menos 20 caracteres. | `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/pipeline/ContextFilter.java` |
| 2 — RF-09 | Pregunta directa con mínimo de longitud que aplica el filtro, termina en `?` y contiene un solo signo `?`. | `.../pipeline/DirectQuestionFilter.java` |
| 3 — RF-10 | Exactamente cinco opciones en total. | `.../pipeline/DistractorCountFilter.java` |
| 4 — RF-12 | Rechaza opciones equivalentes a «Todas las anteriores» y «Ninguna de las anteriores», con normalización indicada por el filtro. | `.../pipeline/ForbiddenExpressionFilter.java` |
| 5 — RF-13 | Opciones no vacías, mínimo 3 caracteres, inicial mayúscula y no duplicadas sin distinguir capitalización. | `.../pipeline/DistractorFormatFilter.java` |
| 6 — RF-11 | Índice correcto válido; opción correcta no vacía ni repetida. | `.../pipeline/CorrectAnswerFilter.java` |
| 7 — RF-05 | Son obligatorios nombre, justificación, bibliografía, competencia, tema, subtema y dificultad. | `.../pipeline/RequiredFieldsFilter.java` |

La secuencia está definida en `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/pipeline/QuestionPipeline.java`. Si una regla falla, la excepción de validación se traduce a HTTP 422 por `banco-service/.../api/ApiExceptionHandler.java`.

## Máquina de estados (State)

Estados implementados: `BORRADOR`, `PENDIENTE_REVISION`, `EN_REVISION`, `APROBADA`, `RECHAZADA`, `PUBLICADA`, `ARCHIVADA`.

| Estado origen | Transiciones válidas |
|---|---|
| `BORRADOR` | `PENDIENTE_REVISION` |
| `PENDIENTE_REVISION` | `EN_REVISION`, `BORRADOR` |
| `EN_REVISION` | `APROBADA`, `RECHAZADA` |
| `RECHAZADA` | `BORRADOR`, `ARCHIVADA` |
| `APROBADA` | `PUBLICADA`, `ARCHIVADA` |
| `PUBLICADA` | `ARCHIVADA` |
| `ARCHIVADA` | Ninguna; estado terminal. |

La jerarquía y las transiciones se encuentran en `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/domain/QuestionState.java`, `QuestionStates.java` y subclases `*State.java`. Los cambios efectivos se guardan en `questions` y se publican para consumidores; las lecturas históricas las ofrece `auditoria-service`.

## Permisos por operación

| Operación | Permiso observado | Aplicación |
|---|---|---|
| Registrar usuario / iniciar sesión | Endpoints de Auth; el registro no exige sesión en la API actual. | `auth-service/.../api/AuthController.java`. |
| Crear y editar pregunta (`POST /preguntas`, `PUT /preguntas/{id}`) | `ADMINISTRADOR`, `AUTOR_PREGUNTAS`. | `banco-service/.../api/QuestionController.java`; `BearerAuthFilter` valida token. |
| Cambiar estado (`PATCH /preguntas/{id}/estado`) | `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`. | `banco-service/.../api/QuestionController.java`. |
| Consultar preguntas, detalle y estadísticas | Se exige token válido por filtro; no se exige rol específico en el controlador. | `banco-service/.../security/BearerAuthFilter.java`, `QuestionController.java`. |
| Gestionar revisión (`/revisiones/...`) | Revisor o administrador según operación; validación de rol en el controlador/servicio. | `revision-service/.../api/ReviewController.java`, `revision-service/.../service/ReviewService.java`. |
| Consultar auditoría | Token válido; no se comparte información de credenciales. | `auditoria-service/.../security/BearerAuthFilter.java`, `.../api/AuditController.java`. |

## Eventos y persistencia

| Código | Regla comprobable | Dónde se aplica |
|---|---|---|
| E-01 | Exchange topic durable `saberpro.events`. | Banco `.../config/AmqpConfig.java`; consumidores `.../config/AmqpConfig.java`. |
| E-02 | Banco publica `question.created` y `question.state-changed`. | `banco-service/.../events/RabbitEventPublisher.java`, `QuestionCreatedEvent.java`, `QuestionStateChangedEvent.java`. |
| E-03 | `auditoria.preguntas` consume `question.*`; Revisión `revision.preguntas` consume `question.state-changed`. | `auditoria-service/.../config/AmqpConfig.java`; `revision-service/.../config/AmqpConfig.java`. |
| E-04 | Consumidores descartan duplicados mediante `eventId` persistido. | `auditoria-service/.../config/AuditConfig.java` (`event_id UNIQUE`); `revision-service/.../config/ReviewConfig.java` (`processed_events`). |
| E-05 | Consumidores reintentan hasta 3 veces; no se declara cola DLQ. Al agotar, `default-requeue-rejected=false`. | `revision-service/src/main/resources/application.properties`; `auditoria-service/src/main/resources/application.properties`. |
| E-06 | Banco puede persistir una operación aun cuando falle publicación del evento; no hay Outbox. | `banco-service/.../service/QuestionService.java`. |
| E-07 | Cada servicio usa una base SQLite propia; no hay FK entre bases de servicios. | `*/config/*Config.java`; `application.properties`; `revision-service/.../config/ReviewConfig.java`; `auditoria-service/.../config/AuditConfig.java`. |

## Errores HTTP relevantes

| Servicio | Situación | Código observado |
|---|---|---|
| Auth | Registro correcto / login correcto / login inválido / duplicado / petición inválida | 201 / 200 / 401 / 409 / 400 |
| Banco | Validación fallida / token ausente o incorrecto / rol insuficiente / pregunta inexistente | 422 / 401 / 403 / 404 |
| Revisión | Token no autorizado / operación no permitida / conflicto de estado / Banco no disponible | 401 / 403 / 409 / 503 |
| Auditoría | Consulta válida / token inválido | 200 / 401 |

Fuentes de traducción HTTP: `*/api/ApiExceptionHandler.java` y `*Controller.java`. Los códigos pueden variar según endpoint; consultar contrato de cada servicio antes de depender de ellos.

## Reglas por decidir

1. Política de asignación de revisores (HU05) y formato completo de evaluación; no están implementados según `docs/decisiones-iteracion-2.md`.
2. Retención/recuperación de mensajes tras agotar los 3 reintentos y necesidad de DLQ.
3. Outbox o mecanismo alternativo de entrega fiable de eventos.
4. Persistencia y despliegue que permitan demostrar escalabilidad con varias instancias.
5. Gestión y rotación del secreto fuera de un entorno de desarrollo.
6. Reglas académicas adicionales de calidad pedagógica no comprobables en los filtros actuales.
7. Validar con el profesor cuáles patrones de diseño cuentan como «los vistos».
