# auditoria-service

Microservicio de **Auditoría e Historial** (puerto `8084`, base de datos propia `auditoria.db`).

## Cómo funciona
- Su cola `auditoria.preguntas` se enlaza al exchange `saberpro.events` con `question.*`: recibe `QuestionCreated` y `QuestionStateChanged`.
- Guarda cada evento (usuario, fecha, estado anterior y nuevo como texto). **Es idempotente**: `event_id` es `UNIQUE`, así que una entrega duplicada del broker no genera un segundo registro.
- Si el servicio está caído, los eventos esperan en la cola y se procesan al volver (consistencia eventual).
- Un mensaje que falla se reintenta 3 veces y luego se descarta, para no bloquear la cola.

## Endpoint
| Método | Ruta | Respuestas |
|---|---|---|
| `GET` | `/auditoria/preguntas/{id}` (con `Authorization: Bearer <token>`) | `200` con la lista cronológica (vacía si no hay registros), `401` |

## Ejecución
```bash
docker compose up -d        # RabbitMQ (desde la raíz)
cd auditoria-service
mvn test
mvn spring-boot:run         # http://localhost:8084
```
