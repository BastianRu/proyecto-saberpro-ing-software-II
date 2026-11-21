# Decisiones de la iteración 2

## 1. Alcance

Historias de usuario HU01 a HU04 (requisitos funcionales RF-01 a RF-15).

- HU05 (revisión por pares) se implementa solo en lo necesario para ejecutar las transiciones `EN_REVISION`, `APROBADA` y `RECHAZADA`. No se implementan RF-16 (asignación de revisores) ni RF-17 (formato de evaluación).
- HU06 a HU08 quedan fuera. Esto incluye RF-29 y RF-30 (trazabilidad de modificaciones de contenido).

## 2. Diferencias respecto al documento de arquitectura inicial

| Aspecto | Documento inicial | Implementado |
|---|---|---|
| Validación estructural | Servicio independiente, invocado por REST desde el Banco | Embebida en `banco-service` (llamada local a la tubería de filtros) |
| Cuarto servicio | Validación Estructural | `revision-service` |
| Consumidores de eventos | Auditoría | Revisión y Auditoría, cada una con su cola |
| `QuestionState` | Enumeración con `allowedTransitions()` | Jerarquía de clases (patrón State, doble despacho) |
| Historial de estados | Tabla en el Banco | `audit_records` en `auditoria-service` |

Justificación de la validación embebida: la tubería es una función pura sobre el borrador. Como servicio independiente agrega una llamada de red síncrona en cada creación y edición, y un punto adicional de falla, sin aislar un dato ni una carga distinta. Las reglas se modifican como una clase `Filter` dentro del Banco.

## 3. Integración con el contenido previo de `main`

`main` contenía `validacion-service` y una versión de `banco-service` conforme al documento inicial. Resolución:

- Se eliminó `validacion-service`. Su tubería de filtros y sus pruebas están en `banco-service`.
- Se reemplazó `banco-service` por la versión de la iteración 2. Los 20 conflictos fueron de tipo `add/add` y solo afectaron a ese directorio.
- Se conservó de la versión reemplazada: la matriz de roles por endpoint, `PATCH /preguntas/{id}/estado`, los datos de ejemplo (ahora opcionales), la estructura de documentación por servicio y el contenido del documento de contexto.
- No se conservó: `GET /preguntas/{id}/historial` (lo sirve `auditoria-service`), el evento `QUESTION_UPDATED`, el formato único de evento con `eventType` (reemplazado por dos eventos tipados con identificador) y el `TokenVerifier` basado en la zona horaria del sistema.

Ramas no integradas, por no pertenecer a HU01 a HU04: `taller10-jpa` (banco con JPA, Taller 10) y `ddd-hexagonal-simulacros` (Taller 11).

## 4. Contratos entre servicios

**Token.** `base64url(login|ROL|vencimiento).base64url(HMAC-SHA256)`. El vencimiento se emite en UTC sin zona explícita. Banco, Revisión y Auditoría lo verifican con el secreto compartido `auth.token.secret`, sin consultar a `auth-service`. Un emisor o verificador que use hora local acepta o rechaza tokens de forma incorrecta.

**Eventos.** Exchange `saberpro.events` (tipo topic). Cada evento incluye `eventId` (UUID), usado por el consumidor para descartar entregas duplicadas.

| Clave de enrutamiento | Campos | Cola consumidora |
|---|---|---|
| `question.created` | `eventId`, `questionId`, `username`, `occurredAt` | `auditoria.preguntas` |
| `question.state-changed` | los anteriores más `previousState`, `newState` | `auditoria.preguntas`, `revision.preguntas` |

Los estados viajan como texto. Los servicios no comparten clases: cada consumidor define su propia copia del contrato y el convertidor JSON infiere el tipo del parámetro del listener, no de la cabecera `__TypeId__`.

**Revisión hacia Banco.** `PATCH /preguntas/{id}/estado` con el token del revisor, de modo que el evento y la auditoría queden a su nombre.

## 5. Control de acceso en el Banco

| Operación | Roles |
|---|---|
| `POST /preguntas`, `PUT /preguntas/{id}` | `ADMINISTRADOR`, `AUTOR_PREGUNTAS` |
| `PATCH /preguntas/{id}/estado` | `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR` |
| Consultas | cualquier usuario autenticado |

Sin token válido se responde 401; con token y sin rol, 403. La autorización de Revisión exige `REVISOR` o `ADMINISTRADOR`. La autorización detallada corresponde a la iteración 3.

## 6. Limitaciones conocidas

- **Pérdida de eventos.** Si el broker no está disponible al persistir un cambio, el cambio se guarda y el evento se descarta con una advertencia en el registro. La corrección es un patrón *outbox* transaccional.
- **Consistencia de Revisión.** Revisión llama al Banco y luego persiste localmente. Si la persistencia local falla después de que el Banco aceptó la transición, la revisión queda desactualizada.
- **SQLite.** El enunciado sugiere MariaDB o Postgres y exige justificar la elección. SQLite con una conexión compartida y métodos `synchronized` no permite varias instancias de un servicio sobre un mismo archivo, lo que limita RNF-17 y RNF-18. Requiere justificación escrita o migración.
- **Eventos de las preguntas de ejemplo.** `banco.sample-data.enabled=true` inserta preguntas sin publicar eventos; una pregunta sembrada en `PENDIENTE_REVISION` no tiene revisión asociada.
- **Cliente JavaFX.** Solo el inicio de sesión usa `auth-service`; el resto aún opera sobre el Banco local del monolito.

## 7. Requisitos pendientes fuera de alcance

- RF-16, RF-17: asignación de revisores y formato de evaluación (HU05).
- RF-29, RF-30: trazabilidad de ediciones de contenido (HU08). Hoy se registran la creación y los cambios de estado.
- RNF-09: eventos de inicio de sesión y de acciones de revisión en la auditoría.

## 8. Ejecución

```bash
docker compose up -d                 # RabbitMQ
cd auth-service      && mvn spring-boot:run   # 8081
cd banco-service     && mvn spring-boot:run   # 8082
cd revision-service  && mvn spring-boot:run   # 8083
cd auditoria-service && mvn spring-boot:run   # 8084
```

Cada servicio documenta sus endpoints en su `README.md`. La colección `postman/saberpro-microservicios.postman_collection.json` recorre el flujo completo.
