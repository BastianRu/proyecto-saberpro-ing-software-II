# Guía de ejecución de los microservicios

Esta guía explica cómo levantar la arquitectura de la iteración 2 y cómo
ejecutar la prueba de punta a punta.

## 1. Requisitos

- JDK 17 o superior (`java -version`).
- Maven 3.8 o superior (`mvn -version`).
- `curl` y `python3` para el script de punta a punta.
- RabbitMQ en `localhost:5672` (opcional en desarrollo; ver sección 6).

No se necesita Docker para ejecutar los servicios: cada uno se levanta con
Maven desde su propia carpeta.

## 2. Puertos

| Componente | Puerto | URL base |
|---|---:|---|
| `auth-service` | 8081 | `http://localhost:8081` |
| `banco-service` | 8082 | `http://localhost:8082` |
| `validacion-service` | 8083 | `http://localhost:8083` |
| RabbitMQ (AMQP) | 5672 | `amqp://localhost:5672` |

## 3. Orden de arranque

El orden recomendado es:

1. RabbitMQ (si está disponible).
2. `auth-service`.
3. `validacion-service`.
4. `banco-service`.

`banco-service` necesita a `validacion-service` para crear o modificar
preguntas, y verifica los tokens emitidos por `auth-service` con el secreto
compartido. No hace falta que `auth-service` esté en marcha para *verificar* un
token, pero sí para emitirlo.

Cada servicio se ejecuta en su propia terminal:

```bash
# Terminal 1 (opcional): RabbitMQ
# Cualquier instalación local o contenedor que exponga el puerto 5672.

# Terminal 2
cd auth-service
mvn spring-boot:run

# Terminal 3
cd validacion-service
mvn spring-boot:run

# Terminal 4
cd banco-service
mvn spring-boot:run
```

También se pueden empaquetar y ejecutar como JAR:

```bash
cd auth-service && mvn -q -DskipTests package
java -jar target/auth-service-1.0-SNAPSHOT.jar
```

El nombre exacto del JAR depende del `artifactId` y la versión de cada
`pom.xml`.

## 4. Comprobación rápida

```bash
# auth-service: lista de usuarios (200)
curl -i http://localhost:8081/usuarios

# validacion-service: un borrador válido (200)
curl -i -X POST http://localhost:8083/validaciones \
  -H 'Content-Type: application/json' \
  -d '{"name":"Prueba","context":"Contexto de mas de veinte caracteres.","directQuestion":"Es valida?","options":["Opcion A","Opcion B","Opcion C","Opcion D","Opcion E"],"correctIndex":0,"justification":"Justificacion","bibliography":"Bibliografia","competence":"Competencia","topic":"Tema","subtopic":"Subtema","difficulty":"MEDIA"}'

# banco-service: listado (200)
curl -i http://localhost:8082/preguntas
```

## 5. Variables de configuración

Cada servicio lee sus valores de `src/main/resources/application.properties`.
Se pueden sobreescribir al arrancar con `-D` o con variables de entorno de
Spring Boot, por ejemplo:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9081"
```

| Servicio | Propiedad | Valor por defecto | Para qué sirve |
|---|---|---|---|
| `auth-service` | `server.port` | `8081` | Puerto HTTP. |
| `auth-service` | `auth.db.url` | `jdbc:sqlite:auth.db` | Base de datos propia. |
| `auth-service` | `auth.token.secret` | `dev-secret-cambiar-en-produccion-0123456789` | Secreto de firma; mínimo 16 caracteres. |
| `auth-service` | `auth.token.ttl-minutes` | `60` | Vigencia del token. |
| `validacion-service` | `server.port` | `8083` | Puerto HTTP. |
| `banco-service` | `server.port` | `8082` | Puerto HTTP. |
| `banco-service` | `banco.db.url` | `jdbc:sqlite:banco.db` | Base de datos propia. |
| `banco-service` | `banco.token.secret` | `dev-secret-cambiar-en-produccion-0123456789` | Debe coincidir con `auth.token.secret`. |
| `banco-service` | `banco.validation.url` | `http://localhost:8083` | URL de `validacion-service`. |
| `banco-service` | `banco.rabbitmq.exchange` | `questions.exchange` | Intercambio de eventos. |
| `banco-service` | `spring.rabbitmq.host` | `localhost` | Host de RabbitMQ. |
| `banco-service` | `spring.rabbitmq.port` | `5672` | Puerto de RabbitMQ. |

> **Importante:** `auth.token.secret` y `banco.token.secret` deben tener el
> mismo valor. Si se cambia solo uno, `banco-service` rechazará con `401` todos
> los tokens emitidos por `auth-service`.

## 6. Ejecución sin RabbitMQ

`banco-service` publica eventos en `questions.exchange`. Si RabbitMQ no está
disponible:

- la creación, modificación y cambio de estado de preguntas **siguen
  funcionando**;
- el publicador registra una advertencia en el log;
- los eventos se pierden, porque todavía no hay consumidores ni cola durable
  que los retenga.

Esto permite ejecutar la prueba de punta a punta sin RabbitMQ. Para verificar la
mensajería completa hay que levantar el broker y observar el intercambio
`questions.exchange` con sus claves `question.question_created`,
`question.question_updated` y `question.question_state_changed`.

## 7. Prueba de punta a punta

Con los tres servicios en marcha:

```bash
./scripts/e2e.sh
```

El script:

1. comprueba que `auth-service` y `banco-service` respondan;
2. registra un administrador y un docente con logins únicos;
3. inicia sesión y obtiene los tokens;
4. crea una pregunta válida (`201`);
5. intenta crear una pregunta inválida (`422`);
6. aplica una transición válida (`200`) y una inválida (`400`);
7. consulta el historial y las estadísticas (`200`);
8. comprueba `401` sin token y `403` con el rol `DOCENTE`;
9. imprime un resumen con cuántas comprobaciones pasaron y fallaron.

Termina con código `0` si todo pasó y `1` si algo falló. Las URLs se pueden
cambiar sin editar el script:

```bash
AUTH_URL=http://localhost:8081 \
BANCO_URL=http://localhost:8082 \
VALIDACION_URL=http://localhost:8083 \
./scripts/e2e.sh
```

## 8. Bases de datos locales

Cada servicio crea su archivo SQLite en el directorio desde el que se ejecuta:

- `auth-service/auth.db` (usuarios);
- `banco-service/banco.db` (preguntas, opciones e historial).

Los archivos `*.db` están ignorados por Git. Para empezar de cero en un entorno
local se pueden detener los servicios y borrar esos archivos; se recrean con el
esquema y, en el banco, con las ocho preguntas de ejemplo.

## 9. Pruebas automatizadas por módulo

```bash
cd auth-service && mvn test
cd ../validacion-service && mvn test
cd ../banco-service && mvn test
```
