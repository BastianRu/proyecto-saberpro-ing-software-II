# Guía de ejecución de los microservicios

Guía para iniciar los servicios que existen en esta iteración. El proyecto mantiene además el cliente JavaFX heredado en `src/`; sus operaciones de Banco aún son locales y no forman parte de este flujo remoto.

## Requisitos

- JDK 17 o superior, Maven 3.8 o superior.
- Docker Compose para levantar RabbitMQ.
- `curl` y `python3` son requeridos únicamente por `scripts/e2e.sh`; dicho script apunta a una arquitectura anterior y no se debe usar como prueba actual.

## Componentes y puertos

| Componente | Puerto | Base de datos / propósito |
|---|---:|---|
| `auth-service` | 8081 | `auth.db`; usuarios y emisión de tokens. |
| `banco-service` | 8082 | `banco.db`; preguntas, validación embebida y estados. |
| `revision-service` | 8083 | `revision.db`; revisión por pares y llamada REST al Banco. |
| `auditoria-service` | 8084 | `auditoria.db`; historial consumido de eventos. |
| RabbitMQ | 5672 | Broker; UI de administración en 15672. |

El único contenedor definido por `docker-compose.yml` es RabbitMQ; los servicios se inician manualmente con Maven. No hay Dockerfiles de aplicaciones.

## Orden recomendado

1. Broker RabbitMQ:
   ```bash
   docker compose up -d
   ```
2. En terminales independientes:
   ```bash
   cd auth-service && mvn spring-boot:run
   cd banco-service && mvn spring-boot:run
   cd revision-service && mvn spring-boot:run
   cd auditoria-service && mvn spring-boot:run
   ```

Revisión requiere Banco disponible cuando toma una revisión o registra un dictamen. Banco publica eventos al exchange `saberpro.events`; las colas `revision.preguntas` y `auditoria.preguntas` son declaradas por sus servicios consumidores. Banco, Revisión y Auditoría verifican localmente el token firmado por Auth, con el mismo secreto compartido.

## Verificación de las pruebas

Ejecutar en cada carpeta de servicio:

```bash
mvn -q test
```

Estado verificado el 2026-11-22: Auth 21, Banco 94, Revisión 40 y Auditoría 9 pruebas ejecutadas; todas pasan. Son los conteos de Maven, no cobertura de líneas.

La colección de Postman vigente es `postman/saberpro-microservicios.postman_collection.json`; sigue el flujo de los cuatro servicios. `scripts/e2e.sh` no está alineado: aún llama al antiguo `validacion-service`, y debe actualizarse antes de ejecutarlo.

## Archivos de datos y configuración

Cada SQLite se crea en el directorio desde el que se inicia su proceso (valores `jdbc:sqlite:*.db` en los `application.properties` respectivos). Los valores de configuración deben revisarse en:

- `auth-service/src/main/resources/application.properties`
- `banco-service/src/main/resources/application.properties`
- `revision-service/src/main/resources/application.properties`
- `auditoria-service/src/main/resources/application.properties`

El secreto compartido tiene valor por defecto de desarrollo. Para un entorno no local, el equipo debe acordar su gestión y rotación; esta guía no describe un despliegue productivo.

## Limitaciones

- No se encontró configuración de escalado horizontal, balanceador, contenedores de aplicación ni prueba de carga.
- SQLite con una conexión compartida y métodos `synchronized` limita concurrencia y no sustenta evidencia de varias instancias compartiendo archivo.
- Los listeners de Revisión y Auditoría tienen reintentos locales de hasta 3 intentos, sin cola DLQ declarada. Al agotarse, el mensaje no se reencola indefinidamente (`*.properties`).
- Si RabbitMQ falla cuando Banco publica, el cambio de negocio puede persistir y el evento perderse; no se encontró Outbox.
- `scripts/e2e.sh` es histórico/desactualizado; no ejecutar como verificación de la arquitectura actual.
