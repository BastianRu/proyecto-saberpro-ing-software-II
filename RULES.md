# Reglas del Banco de Preguntas Saber Pro

> Este archivo describe el comportamiento **real** del código de la rama
> `integracion-iteracion2`. Se reconstruyó el 2026-11-18 leyendo los tres
> servicios, porque `RULES.md` no existía en el repositorio. Si una regla cambia
> en el código, este archivo y los README deben actualizarse en el mismo commit.

Códigos usados:

- `RF-xx`: requisito funcional del pipeline de validación estructural.
- `AUTH-xx`: reglas de usuarios y autenticación.
- `TOK-xx`: formato y uso del token.
- `BQ-xx`: reglas del banco de preguntas y su ciclo de vida.
- `API-xx`: contrato HTTP y manejo de errores.
- `INF-xx`: reglas de infraestructura y mensajería.

## 1. Validación estructural (`validacion-service`)

| Código | Regla | Dónde se aplica |
|---|---|---|
| `RF-08` | El contexto debe existir y, tras recortar espacios, tener al menos 20 caracteres. | `validacion-service/.../pipeline/ContextFilter.java` |
| `RF-09` | La pregunta directa debe existir, tener al menos 10 caracteres, terminar en `?` y contener un solo `?`. | `validacion-service/.../pipeline/DirectQuestionFilter.java` |
| `RF-10` | Deben existir exactamente 5 opciones en total: 1 correcta y 4 distractores. | `validacion-service/.../pipeline/DistractorCountFilter.java` |
| `RF-12` | Ninguna opción puede ser «Todas las anteriores» ni «Ninguna de las anteriores» (comparación sin mayúsculas, recortando espacios y signos finales `. ; ,`). | `validacion-service/.../pipeline/ForbiddenExpressionFilter.java` |
| `RF-13` | Cada opción debe ser no vacía, tener al menos 3 caracteres, iniciar con mayúscula y no repetirse (sin distinguir mayúsculas). | `validacion-service/.../pipeline/DistractorFormatFilter.java` |
| `RF-11` | Debe indicarse un `correctIndex` entre 0 y el número de opciones menos 1; la opción correcta no puede estar vacía ni repetirse en otra posición. | `validacion-service/.../pipeline/CorrectAnswerFilter.java` |
| `RF-05` | Son obligatorios: nombre, justificación, bibliografía, competencia, tema, subtema y nivel de dificultad. | `validacion-service/.../pipeline/RequiredFieldsFilter.java` |
| `VAL-01` | El pipeline ejecuta los filtros en este orden: RF-08 → RF-09 → RF-10 → RF-12 → RF-13 → RF-11 → RF-05. El primer filtro que falla detiene la tubería y devuelve sus errores. | `validacion-service/.../pipeline/QuestionPipeline.java` |
| `VAL-02` | `POST /validaciones` responde `200` si el borrador es válido, `422` si un filtro falla, `400` si el JSON está mal formado, el cuerpo está vacío o `difficulty` es desconocido, y `500` ante un error inesperado. El cuerpo es siempre `{"valid": ..., "errors": [...]}`. | `validacion-service/.../api/ValidationController.java`, `ApiExceptionHandler.java` |
| `VAL-03` | Los valores admitidos en `difficulty` son `BAJA`, `MEDIA` y `ALTA`. | `validacion-service/.../domain/DifficultyLevel.java` |
| `VAL-04` | El servicio no tiene base de datos, no es dueño de datos y no llama a otros servicios. | `validacion-service/pom.xml` y paquete `api` |

## 2. Usuarios y autenticación (`auth-service`)

| Código | Regla | Dónde se aplica |
|---|---|---|
| `AUTH-01` | El `login` es único en el sistema. Un registro duplicado responde `409`. | `SQLiteUserRepository`, tabla `users.login UNIQUE`, `UserService.register` |
| `AUTH-02` | La contraseña debe tener al menos 6 caracteres, un dígito, una mayúscula y un carácter especial. Si no cumple, responde `400` con la lista de errores. | `auth-service/.../domain/PasswordPolicy.java`, `UserService.register` |
| `AUTH-03` | La contraseña se guarda con Argon2id (2 iteraciones, 64 MB, paralelismo 1) y nunca se devuelve en las respuestas. | `Argon2PasswordHasher`, `AuthController.UserResponse` |
| `AUTH-04` | El `login` no puede contener el carácter `\|`, porque es el separador del token. | `UserService.register` |
| `AUTH-05` | Un usuario registrado queda en estado `ACTIVO`. Un usuario `INACTIVO` no puede iniciar sesión. | `UserService.register`, `UserService.login` |
| `AUTH-06` | Si el usuario no existe, está inactivo o la contraseña es incorrecta, el login responde `401` con el mismo mensaje, para no revelar cuál falló. | `UserService.login`, `ApiExceptionHandler` |
| `AUTH-07` | `POST /usuarios` responde `201` con `id`, `login`, `fullName`, `role` y `status`; `POST /login` responde `200` con `token`, `subject`, `role` y `expiresAt`; `GET /usuarios` responde `200` sin el hash. | `auth-service/.../api/AuthController.java` |
| `AUTH-08` | Los roles admitidos son `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE` y `ESTUDIANTE`. Un rol desconocido produce `400`. | `auth-service/.../domain/Role.java`, `ApiExceptionHandler` |

## 3. Token (`auth-service` y `banco-service`)

| Código | Regla | Dónde se aplica |
|---|---|---|
| `TOK-01` | El token tiene el formato `base64url(subject \| role \| expiresAt).base64url(firma)`, sin relleno `=` en ninguna de las dos partes. | `auth-service/.../service/TokenSigner.java`, `banco-service/.../security/TokenVerifier.java` |
| `TOK-02` | La firma es HMAC-SHA256 sobre el payload `subject \| role \| expiresAt` usando el secreto compartido. | `TokenSigner.sign`, `TokenVerifier.sign` |
| `TOK-03` | `expiresAt` es un `LocalDateTime` ISO-8601 sin zona (por ejemplo `2026-11-04T13:00`). El token vence cuando `ahora >= expiresAt`. | `TokenSigner.issue`, `AuthToken.isExpired`, `TokenVerifier.verify` |
| `TOK-04` | La vigencia por defecto es de 60 minutos (`auth.token.ttl-minutes`). | `auth-service/src/main/resources/application.properties`, `AuthConfig` |
| `TOK-05` | El secreto debe tener al menos 16 caracteres. `auth.token.secret` y `banco.token.secret` deben tener el mismo valor. | `TokenSigner`, `TokenVerifier`, `AuthConfig`, `BancoConfig` |
| `TOK-06` | `banco-service` verifica la firma y la vigencia localmente; no llama a `auth-service` para validar el token. | `banco-service/.../security/TokenVerifier.java` |
| `TOK-07` | El encabezado debe ser `Authorization: Bearer <token>`. El verificador tolera que se pase el token sin el prefijo `Bearer `. | `TokenVerifier.verify` |
| `TOK-08` | Hay un vector fijo compartido en `TokenSignerTest` (auth) y `TokenVerifierTest` (banco) para detectar cambios incompatibles en el formato. | `auth-service/.../TokenSignerTest.java`, `banco-service/.../TokenVerifierTest.java` |

## 4. Banco de preguntas (`banco-service`)

| Código | Regla | Dónde se aplica |
|---|---|---|
| `BQ-01` | El identificador de una pregunta tiene el formato `P-###` y se asigna como el máximo existente más uno (`P-001`, `P-002`, ...). | `QuestionService.nextId` |
| `BQ-02` | Antes de crear o modificar una pregunta se llama a `validacion-service`. Si las reglas fallan, no se persiste nada. | `QuestionService.createQuestion`, `QuestionService.updateQuestion`, `RestValidationClient` |
| `BQ-03` | Una pregunta nueva se crea en estado `BORRADOR`, registra el historial inicial (`null → BORRADOR`) y publica `QUESTION_CREATED`. | `QuestionService.createQuestion` |
| `BQ-04` | Solo se pueden modificar preguntas en estado `BORRADOR`. Cualquier otro estado responde `400`. | `QuestionService.updateQuestion`, `Question.modifyContent` |
| `BQ-05` | Las transiciones válidas son: `BORRADOR → PENDIENTE_REVISION`; `PENDIENTE_REVISION → EN_REVISION, BORRADOR`; `EN_REVISION → APROBADA, RECHAZADA`; `RECHAZADA → BORRADOR, ARCHIVADA`; `APROBADA → PUBLICADA, ARCHIVADA`; `PUBLICADA → ARCHIVADA`; `ARCHIVADA` no permite salida. | `banco-service/.../domain/QuestionState.java` |
| `BQ-06` | Una transición no permitida responde `400`. Cambiar al mismo estado es un no-op: responde `200`, no escribe historial y no publica evento. | `Question.changeState`, `QuestionService.changeState` |
| `BQ-07` | Cada transición efectiva guarda en `question_state_history` el estado origen, el destino, el usuario y la fecha, y publica `QUESTION_STATE_CHANGED`. | `QuestionService.changeState`, `SQLiteQuestionRepository.saveStateChange` |
| `BQ-08` | No existe borrado físico de preguntas. Una pregunta se desactiva pasando a `ARCHIVADA`, que es estado final. | `QuestionState`, `IQuestionRepository` (no hay `delete`) |
| `BQ-09` | `PUT /preguntas/{id}` publica `QUESTION_UPDATED`; `POST /preguntas` publica `QUESTION_CREATED`; `PATCH /preguntas/{id}/estado` publica `QUESTION_STATE_CHANGED`. | `QuestionService`, `QuestionEvent` |
| `BQ-10` | `GET /preguntas` acepta los filtros opcionales `state`, `difficulty`, `competence`, `topic`, `subtopic` y `text`. `text` busca, sin distinguir mayúsculas, en nombre, contexto y enunciado. | `QuestionController.list`, `QuestionCriteria.matches` |
| `BQ-11` | `GET /preguntas/{id}` responde `404` si la pregunta no existe. `GET /preguntas/{id}/historial` devuelve una lista vacía si no hay registros. | `QuestionController.findById`, `QuestionService.stateHistory` |
| `BQ-12` | `GET /preguntas/estadisticas` devuelve los conteos de los 7 estados (incluidos los que están en cero), sus porcentajes y el total. | `QuestionStatistics`, `QuestionStatisticsResponse` |
| `BQ-13` | Si la base `banco.db` está vacía, se cargan 8 preguntas de ejemplo. | `QuestionSampleData.loadIfEmpty`, `BancoConfig` |
| `BQ-14` | La base `banco.db` es propia del servicio y contiene `questions`, `question_options` y `question_state_history`. | `BancoConfig.initializeSchema`, `SQLiteQuestionRepository` |

## 5. Seguridad del banco y contrato HTTP

| Código | Regla | Dónde se aplica |
|---|---|---|
| `API-01` | `GET` de preguntas, historial y estadísticas no exige token. | `QuestionController` |
| `API-02` | `POST /preguntas` y `PUT /preguntas/{id}` exigen un token válido con rol `ADMINISTRADOR` o `AUTOR_PREGUNTAS`. | `QuestionController.requireUser` |
| `API-03` | `PATCH /preguntas/{id}/estado` exige un token válido con rol `ADMINISTRADOR`, `AUTOR_PREGUNTAS` o `REVISOR`. | `QuestionController.requireUser` |
| `API-04` | Sin encabezado `Authorization`, con token inválido o expirado, la respuesta es `401`. Con un rol no permitido, es `403`. | `QuestionController.requireUser`, `UnauthorizedException`, `ForbiddenException` |
| `API-05` | Códigos del banco: `400` para cuerpo o transición inválida; `401` sin token o token inválido; `403` rol sin permiso; `404` pregunta inexistente; `422` reglas de validación incumplidas; `503` validación no disponible; `500` error inesperado. | `banco-service/.../api/ApiExceptionHandler.java` |
| `API-06` | El cuerpo de error general es `{"error": "...", "details": []}`. Los errores de reglas conservan `{"valid": false, "errors": [...]}`. | `ApiExceptionHandler`, `RestValidationClient` |
| `API-07` | Si `validacion-service` no responde en 5 segundos, está caído o la petición se interrumpe, el banco responde `503` (`ValidationServiceUnavailableException`). | `RestValidationClient`, `ApiExceptionHandler` |
| `API-08` | Si `validacion-service` responde `422`, el banco responde `422` con los mensajes remotos. | `RestValidationClient`, `ApiExceptionHandler` |

## 6. Mensajería e infraestructura

| Código | Regla | Dónde se aplica |
|---|---|---|
| `EVT-01` | El intercambio es `questions.exchange`, de tipo tema y durable. | `BancoConfig.questionsExchange`, `application.properties` |
| `EVT-02` | La clave de enrutamiento es `question.` más el tipo de evento en minúsculas: `question.question_created`, `question.question_updated`, `question.question_state_changed`. | `RabbitMQQuestionEventPublisher.publish` |
| `EVT-03` | Los eventos llevan `eventType`, `questionId`, `fromState`, `toState`, `username` y `timestamp`. | `banco-service/.../messaging/QuestionEvent.java` |
| `EVT-04` | Si RabbitMQ falla, se registra una advertencia y la operación de negocio continúa. El evento se pierde. | `RabbitMQQuestionEventPublisher.publish` |
| `EVT-05` | Hoy **no hay consumidores** de los eventos ni colas declaradas por los servicios. Está anotado en `docs/PENDIENTES.md`. | Repositorio completo (verificado con `grep`) |
| `INF-01` | Cada servicio es un módulo Maven independiente con su propio `pom.xml` y su propio puerto: auth 8081, banco 8082, validación 8083. | `*/pom.xml`, `*/src/main/resources/application.properties` |
| `INF-02` | Cada servicio tiene su propia base SQLite (`auth.db`, `banco.db`); no hay base compartida. Validación no tiene base. | `AuthConfig`, `BancoConfig`, `application.properties` |
| `INF-03` | No se comparten clases entre servicios mediante una librería común; cada uno conserva su copia mínima de los contratos. | Estructura de paquetes y `docs/REDUNDANCIAS.md` |
| `INF-04` | La validación es síncrona por HTTP; los eventos son asíncronos por RabbitMQ. | `RestValidationClient`, `RabbitMQQuestionEventPublisher` |
