# Taller 12 - Autenticación y autorización con Keycloak, Spring Boot y Java Swing

Laboratorio de Ingeniería de Software II (Periodo 2-2026), Universidad del Cauca.

Sistema distribuido con autenticación y autorización basadas en JWT:

```
Cliente Swing --(usuario/clave)--> Keycloak :8080 (realm sistema, client sistema-desktop)
      |                                  |
      |<------------- JWT (roles) -------|
      |
      +--(Authorization: Bearer JWT)--> API Gateway :8081 --/a/**--> microservice-a :8082
                                              |             --/b/**--> microservice-b :8083
                                     valida JWT + rol          valida JWT + rol (otra vez)
```

| Módulo | Puerto | Tecnología |
|---|---|---|
| `keycloak/` | 8080 | Keycloak 25 (binario o Docker) + scripts `setup-realm.sh` y `verify-parte3.sh` |
| `backend/api-gateway` | 8081 | Spring Boot 3.4.5, Spring Cloud Gateway 2024.0.1 (WebFlux) |
| `backend/microservice-a` | 8082 | Spring Boot 3.4.5 (MVC), resource server |
| `backend/microservice-b` | 8083 | Spring Boot 3.4.5 (MVC), resource server |
| `desktop-client` | - | Java 17, Swing, `java.net.http` |

Requisitos: Java 17 y Maven 3.8+.

## 1. Keycloak

```bash
# Con el zip binario de Keycloak (https://www.keycloak.org/downloads):
KEYCLOAK_ADMIN=admin KEYCLOAK_ADMIN_PASSWORD=admin <keycloak>/bin/kc.sh start-dev --http-port=8080

# En otra terminal, crea realm, cliente, roles y usuarios (Parte 1 de la guía):
KEYCLOAK_HOME=<keycloak> keycloak/setup-realm.sh
```

Crea el realm `sistema`, el cliente `sistema-desktop` (Direct access grants), los roles de cliente
`student`, `coordinator`, `company` y los usuarios `juan` (student), `julio` (coordinator) y
`company1` (company), todos con contraseña `123`.

## 2. Backend y cliente

```bash
(cd backend/microservice-a && mvn spring-boot:run)   # :8082
(cd backend/microservice-b && mvn spring-boot:run)   # :8083
(cd backend/api-gateway     && mvn spring-boot:run)   # :8081
(cd desktop-client          && mvn exec:java)         # Swing
```

Las URLs se pueden cambiar sin recompilar: `KEYCLOAK_ISSUER_URI`, `MICROSERVICE_A_URL`,
`MICROSERVICE_B_URL` (backend) y `-Dkeycloak.token.url`, `-Dgateway.url`, `-Dkeycloak.client.id` (cliente).

## 3. Casos de la Parte 3

```bash
keycloak/verify-parte3.sh   # compara el estado HTTP esperado y el obtenido en cada caso
```

| Usuario | Endpoint | Esperado |
|---|---|---|
| juan | `/a/hello_student` | 200 |
| juan | `/a/hello_coordinator` | 403 |
| julio | `/b/hello_coordinator` | 200 |
| company1 | `/b/hello_company` | 200 |
| cualquiera con token | `/b/hello_all` | 200 |
| sin token | cualquiera | 401 |

## 4. Pruebas automatizadas (no requieren Keycloak)

```bash
for m in backend/microservice-a backend/microservice-b backend/api-gateway desktop-client; do (cd $m && mvn test); done
```

87 pruebas: 25 (A) + 25 (B) + 22 (gateway) + 15 (cliente). Usan la cadena de seguridad real de Spring;
solo se sustituye el decodificador JWT y los servicios remotos por simulaciones locales.

## 5. Informe

`docs/informe/informe.md` es la fuente; `docs/informe/build.sh` genera el `.docx` y el `.pdf`
(requiere pandoc, python3 y LibreOffice). Las capturas están en `docs/evidencias/`.

## Decisiones de diseño relevantes

- **Roles de Keycloak -> Spring Security:** Keycloak guarda los roles de cliente en
  `resource_access.sistema-desktop.roles`; `KeycloakRoleConverter` los convierte en autoridades `ROLE_*`.
- **Defensa en profundidad:** el gateway y cada microservicio validan el JWT y el rol.
- **`hello_all`** exige token válido pero no un rol específico (la guía pide 401 sin token en cualquier endpoint).
- **Denegar por defecto:** las rutas no declaradas responden 403.
- `grant_type=password` se usa solo porque la guía lo pide; en producción corresponde Authorization Code + PKCE.
