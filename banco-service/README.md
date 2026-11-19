# banco-service

Microservicio del **Banco de Preguntas** (puerto `8082`, base de datos propia `banco.db`).

Gestiona el ciclo de vida de las preguntas de selección múltiple con única respuesta. **La validación estructural
(HU03) está embebida**: la tubería de filtros corre en el mismo proceso, sin llamada de red ni servicio aparte.

## Responsabilidades

1. **Preguntas (HU02):** crear, consultar con filtros y modificar (solo en Borrador).
2. **Validación estructural (HU03):** pipeline de 7 filtros (RF-05, RF-08 a RF-13). Si falla, responde `422` con la lista de errores y no guarda.
3. **Ciclo de vida (HU04):** 7 estados implementados con el **patrón State** (una clase por estado, doble despacho; sin `switch` ni tabla de transiciones).
4. **Eventos:** publica `QuestionCreated` y `QuestionStateChanged` en RabbitMQ (exchange `saberpro.events`). Los consumen Revisión y Auditoría.
5. **Seguridad (HU01/RF-03):** verifica localmente el token firmado por `auth-service` y aplica la matriz de roles.

## Matriz de roles

| Operación | Roles |
|---|---|
| `POST /preguntas`, `PUT /preguntas/{id}` | `ADMINISTRADOR`, `AUTOR_PREGUNTAS` |
| `PATCH /preguntas/{id}/estado` | `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR` (los revisores lo hacen vía `revision-service`) |
| `GET` (consultas y estadísticas) | cualquier usuario autenticado |

Sin token válido: `401`. Con token pero sin el rol: `403`.

## Endpoints

| Método | Ruta | Respuestas |
|---|---|---|
| `POST` | `/preguntas` | `201`, `422` (validación), `403` |
| `GET` | `/preguntas?state&difficulty&competence&topic&subtopic&text` | `200` |
| `GET` | `/preguntas/{id}` | `200`, `404` |
| `PUT` | `/preguntas/{id}` | `200`, `409` (no está en Borrador), `422`, `404` |
| `PATCH` | `/preguntas/{id}/estado` — cuerpo `{"state":"PENDIENTE_REVISION"}` | `200`, `409` (transición inválida), `400` (estado desconocido), `404` |
| `GET` | `/preguntas/estadisticas` | `200` |

Todas exigen `Authorization: Bearer <token>`. El historial de cambios de estado **ya no está aquí**: lo sirve `auditoria-service`.

## Máquina de estados

```
BORRADOR → PENDIENTE_REVISION → EN_REVISION → APROBADA → PUBLICADA → ARCHIVADA
              ↓ (devolver)          ↓                ↘ ARCHIVADA
           BORRADOR              RECHAZADA → BORRADOR | ARCHIVADA
```
`ARCHIVADA` es final. No existe borrado físico (RNF-16).

## Ejecución

```bash
docker compose up -d        # RabbitMQ (desde la raíz del repositorio)
cd banco-service
mvn test
mvn spring-boot:run         # http://localhost:8082
```

Configuración en `application.properties`: `banco.db.url`, `auth.token.secret` (el mismo de `auth-service`), RabbitMQ y
`banco.sample-data.enabled` (preguntas de ejemplo para demos; apagado por defecto porque no publica eventos).

Colección de Postman con todo el flujo: `postman/saberpro-microservicios.postman_collection.json` (en la raíz).

## Límites conocidos
- Si RabbitMQ está caído al guardar, el cambio se persiste pero el evento se pierde (se registra una advertencia). La solución completa sería un *outbox* transaccional.
- SQLite con una única conexión compartida: no admite varias instancias del servicio sobre el mismo archivo.
