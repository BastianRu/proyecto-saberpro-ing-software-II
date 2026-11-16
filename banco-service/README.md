# banco-service

Microservicio del **Banco de Preguntas** del proyecto *Banco de Preguntas Saber Pro* (Ingeniería de Software II, Universidad del Cauca).

Gestiona el ciclo de vida completo de las preguntas de selección múltiple con única respuesta: creación con validación estructural delegada, modificación restringida a borradores, transiciones controladas por máquina de estados, persistencia en SQLite (`banco.db`), registro histórico de auditoría y publicación de eventos asíncronos en RabbitMQ.

## Responsabilidades del Servicio

1. **Gestión de Preguntas:** Crear, consultar y modificar preguntas con soporte de 5 opciones (4 distractores y 1 correcta) y metadatos pedagógicos.
2. **Validación Estructural Delegada:** Invoca síncronamente al microservicio `validacion-service` (`POST /validaciones` en `http://localhost:8083`) antes de persistir cualquier borrador.
3. **Máquina de Estados y Auditoría (HU04):** Aplica las transiciones válidas entre los 7 estados (`BORRADOR`, `PENDIENTE_REVISION`, `EN_REVISION`, `APROBADA`, `RECHAZADA`, `PUBLICADA`, `ARCHIVADA`), registrando el usuario y la fecha en `question_state_history`.
4. **Seguridad y Control de Acceso:** Verifica la firma HMAC-SHA256 de los tokens emitidos por `auth-service` y autoriza operaciones según el rol (`ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, etc.).
5. **Publicación Asíncrona de Eventos:** Publica eventos de dominio (`QUESTION_CREATED`, `QUESTION_UPDATED`, `QUESTION_STATE_CHANGED`) en RabbitMQ (`questions.exchange`).

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- Microservicio `validacion-service` ejecutándose en el puerto 8083.
- (Opcional en desarrollo) RabbitMQ ejecutándose en `localhost:5672`.

## Ejecución

Desde la carpeta `banco-service`:

```bash
# Compilar y ejecutar pruebas
mvn test

# Iniciar el microservicio
mvn spring-boot:run
```

El servicio queda escuchando en `http://localhost:8082`.

## Endpoints de la API REST

### 1. `GET /preguntas`
Consulta y filtra preguntas del banco.
- **Parámetros opcionales de consulta:** `state`, `difficulty`, `competence`, `topic`, `subtopic`, `text`.
- **Respuesta:** `200 OK` con la lista de `QuestionResponse`.

### 2. `GET /preguntas/{id}`
Consulta el detalle de una pregunta por su identificador (ej. `P-001`).
- **Respuesta:** `200 OK` o `404 Not Found`.

### 3. `POST /preguntas`
Crea una nueva pregunta en estado `BORRADOR`.
- **Encabezado obligatorio:** `Authorization: Bearer <token>`
- **Roles permitidos:** `ADMINISTRADOR`, `AUTOR_PREGUNTAS`.
- **Cuerpo JSON:**
```json
{
  "name": "Pregunta sobre Pipeline",
  "context": "El patron Pipeline encadena filtros que procesan datos en secuencia.",
  "directQuestion": "Cual es la ventaja principal de este patron?",
  "options": [
    "Permite componer validaciones independientes",
    "Obliga a reescribir todo el sistema",
    "Elimina la necesidad de pruebas",
    "Impide reutilizar componentes",
    "Requiere una base de datos distribuida"
  ],
  "correctIndex": 0,
  "justification": "Permite componer filtros desacoplados.",
  "bibliography": "Gamma et al. Design Patterns.",
  "competence": "Diseno de software",
  "topic": "Patrones de diseno",
  "subtopic": "Pipeline",
  "difficulty": "MEDIA"
}
```
- **Respuesta:** `201 Created` con la pregunta creada y su id asignado (ej. `P-009`).

### 4. `PUT /preguntas/{id}`
Modifica los datos de una pregunta existente en estado `BORRADOR`.
- **Encabezado obligatorio:** `Authorization: Bearer <token>`
- **Roles permitidos:** `ADMINISTRADOR`, `AUTOR_PREGUNTAS`.
- **Respuesta:** `200 OK` o `400 Bad Request` si la pregunta ya no está en `BORRADOR`.

### 5. `PATCH /preguntas/{id}/estado`
Aplica una transición de estado en el ciclo de vida de la pregunta.
- **Encabezado obligatorio:** `Authorization: Bearer <token>`
- **Roles permitidos:** `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`.
- **Cuerpo JSON:**
```json
{
  "state": "PENDIENTE_REVISION"
}
```
- **Respuesta:** `200 OK` con el nuevo estado aplicado.

### 6. `GET /preguntas/{id}/historial`
Consulta el historial cronológico de auditoría de cambios de estado.
- **Respuesta:** `200 OK` con la lista de transiciones (`fromState`, `toState`, `username`, `changedAt`, `description`).

### 7. `GET /preguntas/estadisticas`
Devuelve el conteo y porcentajes de preguntas agrupadas por estado.
- **Respuesta:** `200 OK` con conteos individuales y totales.

---

## Estructura del Proyecto

```
banco-service/
├── pom.xml
├── README.md
├── postman/
│   └── banco-service.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/unicauca/lisw2t2g08/banco/
    │   │   ├── BancoServiceApplication.java
    │   │   ├── access/        (IQuestionRepository, SQLiteQuestionRepository, QuestionSampleData)
    │   │   ├── api/           (QuestionController, ApiExceptionHandler, DTOs)
    │   │   ├── client/        (IValidationClient, RestValidationClient, ValidationException)
    │   │   ├── config/        (BancoConfig - SQLite, Beans, RabbitMQ)
    │   │   ├── domain/        (Question, QuestionDraft, QuestionState, QuestionDistractors, etc.)
    │   │   ├── messaging/     (QuestionEvent, IQuestionEventPublisher, RabbitMQQuestionEventPublisher)
    │   │   └── security/      (TokenVerifier, UnauthorizedException, ForbiddenException)
    │   └── resources/
    │       └── application.properties
    └── test/java/com/unicauca/lisw2t2g08/banco/
        ├── access/            (SQLiteQuestionRepositoryTest)
        ├── api/               (QuestionControllerTest)
        ├── security/          (TokenVerifierTest)
        └── service/           (QuestionServiceTest)
```
