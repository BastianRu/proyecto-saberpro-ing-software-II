# revision-service

Microservicio de **Revisión por pares** (puerto `8083`, base de datos propia `revision.db`).

## Cómo funciona

1. Consume `QuestionStateChanged` de RabbitMQ. Cuando una pregunta pasa a `PENDIENTE_REVISION`, crea una **revisión pendiente**. Si el autor la devuelve a `BORRADOR`, la cancela.
2. Un `REVISOR` (o `ADMINISTRADOR`) **toma** la revisión: este servicio llama por REST al Banco (`PATCH /preguntas/{id}/estado`, con el token del revisor) para pasar la pregunta a `EN_REVISION`.
3. El mismo revisor emite el **dictamen** (aprobar o rechazar con observaciones): el Banco pasa la pregunta a `APROBADA` o `RECHAZADA`.

El Banco sigue siendo dueño del ciclo de vida: Revisión nunca cambia un estado por su cuenta.

## Reglas
- Un revisor no puede revisar una pregunta que él mismo envió (revisión "por pares").
- Solo se toma una revisión pendiente; solo el revisor que la tomó emite el dictamen.
- Rechazar exige observaciones.
- Se validan las reglas locales **antes** de llamar al Banco; si el Banco rechaza o no responde, la revisión no cambia.
- El procesamiento de eventos es idempotente (tabla `processed_events`).

## Endpoints (todos con `Authorization: Bearer <token>`)

| Método | Ruta | Respuestas |
|---|---|---|
| `GET` | `/revisiones?status=PENDIENTE\|EN_CURSO\|COMPLETADA\|CANCELADA` | `200`, `400` |
| `GET` | `/revisiones/{id}` | `200`, `404` |
| `GET` | `/revisiones/preguntas/{questionId}` | `200` (historial de revisiones) |
| `POST` | `/revisiones/{id}/tomar` | `200`, `403`, `409`, `503` (Banco caído) |
| `POST` | `/revisiones/{id}/dictamen` — `{"decision":"APROBAR\|RECHAZAR","observations":"..."}` | `200`, `400`, `403`, `409` |

## Ejecución
```bash
docker compose up -d        # RabbitMQ (desde la raíz)
cd revision-service
mvn test
mvn spring-boot:run         # http://localhost:8083 (el Banco debe estar en 8082)
```
