# auth-service

Microservicio de **Usuarios y Autenticacion** (HU01) del proyecto *Banco de
Preguntas Saber Pro* (Ingenieria de Software II, Universidad del Cauca).

Registra usuarios con contrasenas cifradas, valida credenciales y emite tokens
firmados que los demas servicios verifican localmente. Es el dueno de su propia
base de datos SQLite (`auth.db`); no consulta a los otros servicios.

## Responsabilidades del servicio

1. **Registro de usuarios:** login unico, nombre completo, rol y contrasena.
2. **Politica de contrasenas:** minimo 6 caracteres, al menos un digito, una
   mayuscula y un caracter especial.
3. **Cifrado:** la contrasena se guarda con Argon2id; nunca en texto plano ni en
   las respuestas HTTP.
4. **Inicio de sesion:** verifica las credenciales y devuelve un token firmado
   con HMAC-SHA256.
5. **Listado de usuarios:** consulta de los usuarios registrados, sin exponer el
   hash de la contrasena.

**Roles admitidos:** `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE` y
`ESTUDIANTE`.

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- Conexion a internet la primera vez, para descargar las dependencias de Spring
  Boot, SQLite y Argon2.

## Compilar y probar

Desde la carpeta `auth-service`:

```bash
mvn test
```

Ejecuta 21 pruebas: contrato REST con `MockMvc` (6), emision y verificacion del
token (7) y logica de usuarios (8). Entre ellas hay una prueba de contrato con
un vector fijo del token, compartida con `banco-service`, para detectar cambios
incompatibles en el formato.

## Ejecutar

```bash
mvn spring-boot:run
```

El servicio queda escuchando en `http://localhost:8081` y crea (o reutiliza)
`auth.db` en el directorio desde el que se ejecuta.

Variables de configuracion en `src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Uso |
|---|---|---|
| `server.port` | `8081` | Puerto HTTP. |
| `auth.db.url` | `jdbc:sqlite:auth.db` | Base de datos propia del servicio. |
| `auth.token.secret` | `dev-secret-cambiar-en-produccion-0123456789` | Secreto compartido con `banco-service`; debe tener al menos 16 caracteres. |
| `auth.token.ttl-minutes` | `60` | Vigencia del token en minutos. |

> En un despliegue real hay que cambiar el secreto y usar el mismo valor en
> `auth-service` (`auth.token.secret`) y en `banco-service`
> (`banco.token.secret`).

## Contrato de la API REST

Los errores de negocio usan el cuerpo
`{"message": "...", "errors": ["..."]}`.

### 1. `POST /usuarios`

Registra un usuario activo.

```json
{
  "login": "ana",
  "fullName": "Ana Perez",
  "role": "AUTOR_PREGUNTAS",
  "password": "Clave123!"
}
```

- **Respuesta:** `201 Created` con `id`, `login`, `fullName`, `role` y `status`.
- **Errores:** `400` si la contrasena no cumple la politica, el rol no existe o
  el cuerpo esta mal formado; `409` si el login ya existe.

### 2. `POST /login`

```json
{
  "login": "ana",
  "password": "Clave123!"
}
```

- **Respuesta:** `200 OK`

```json
{
  "token": "<payload>.<firma>",
  "subject": "ana",
  "role": "AUTOR_PREGUNTAS",
  "expiresAt": "2026-11-18T22:30:00"
}
```

- **Errores:** `401` si el usuario no existe, esta inactivo o la contrasena es
  incorrecta. El mensaje es el mismo en los tres casos.

El token se envia a los demas servicios como
`Authorization: Bearer <token>`.

### 3. `GET /usuarios`

- **Respuesta:** `200 OK` con la lista de usuarios, sin `hashedPassword`.

### Formato del token

```
base64url(subject|role|expiresAt).base64url(HMAC-SHA256(payload, secreto))
```

- `base64url` sin relleno (`=`).
- `expiresAt` es un `LocalDateTime` en formato ISO-8601, por ejemplo
  `2026-11-04T13:00`.
- El login no puede contener el caracter `|`.
- `banco-service` verifica la firma y la vigencia sin llamar a este servicio.

## Coleccion Postman

La coleccion `postman/auth-service.postman_collection.json` contiene registro,
inicio de sesion y listado. El login guarda el token en la variable de coleccion
`token` para reutilizarlo. Importala en Postman y ejecutala contra
`http://localhost:8081`.

## Estructura

```
auth-service/
├── pom.xml
├── README.md
├── postman/
│   └── auth-service.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/unicauca/lisw2t2g08/auth/
    │   │   ├── AuthServiceApplication.java
    │   │   ├── access/    (IUserRepository, SQLiteUserRepository, Argon2PasswordHasher)
    │   │   ├── api/       (AuthController, ApiExceptionHandler, DTOs)
    │   │   ├── config/    (AuthConfig - base de datos, beans y TokenSigner)
    │   │   ├── domain/    (User, Role, UserStatus, PasswordPolicy, AuthToken)
    │   │   └── service/   (IUserService, UserService, TokenSigner y excepciones)
    │   └── resources/
    │       └── application.properties
    └── test/java/com/unicauca/lisw2t2g08/auth/
        ├── api/       (AuthControllerTest)
        └── service/   (TokenSignerTest, UserServiceTest, FakeUserRepository)
```
