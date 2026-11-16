# Contexto y Documentación Técnica del Proyecto: Banco de Preguntas Saber Pro

> **Asignatura:** Laboratorio de Ingeniería de Software II (2026.2)  
> **Institución:** Universidad del Cauca — Facultad de Ingeniería Electrónica y Telecomunicaciones, Popayán  
> **Equipo Desarrollador:** *"Sin Nombre"*  
> **Integrantes:** Juan Manuel Meneses Daza, Juan Sebastian Muñoz Ruiz, Juan Sebastian Molano Bolaños, Santiago Rodríguez Bolaños  
> **Repositorio:** [github.com/BastianRu/proyecto-saberpro-ing-software-II](https://github.com/BastianRu/proyecto-saberpro-ing-software-II)  
> **Gestor de Tareas:** Jira Software ([Tablero Jira](https://unicauca-team-vpqtq3w8.atlassian.net/))

---

## 1. Visión General del Proyecto

El **Banco de Preguntas Saber Pro** es un sistema diseñado para la gestión integral del ciclo de vida de preguntas de selección múltiple con única respuesta, orientadas a la preparación y entrenamiento de estudiantes del programa de Ingeniería de Sistemas en las pruebas de Estado Saber Pro.

El proyecto aborda desde el registro seguro y control de acceso basado en roles de los usuarios, la formulación estricta de preguntas con validaciones de calidad pedagógica y técnica, la gestión de estados a través de un flujo formal de revisión y aprobación con trazabilidad y auditoría, hasta la visualización reactiva de estadísticas y métricas del banco.

---

## 2. Evolución Arquitectónica

El proyecto está concebido en fases o iteraciones académicas estructuradas:

```
[Iteración 1: Monolito en Capas] ────────> [Iteración 2: Arquitectura de Microservicios]
  • Java SE 17 + JavaFX 21                   • Spring Boot 3.3.5
  • SQLite monolítico (taller2.db)           • Descentralización de datos (Database-per-service)
  • Patrones: MVC, Observer,                 • Comunicación síncrona REST + asíncrona (RabbitMQ)
    Pipeline, State, Inversión Dep.          • auth-service, validacion-service, banco-service
```

### Iteración 1: Monolito en Capas y Patrones de Diseño
* **Enfoque:** Monolito local de escritorio en Java SE 17 con interfaz gráfica JavaFX 21 y base de datos relacional embebida SQLite (`taller2.db`).
* **Objetivo de Diseño:** Demostrar el dominio manual de principios **SOLID** (con énfasis en el Principio de Inversión de Dependencias - DIP) y patrones de diseño GoF y arquitectónicos sin delegar la lógica a contenedores mágicos de frameworks.
* **Separación de Capas:**
  - `presentation / ui`: Vistas JavaFX y controladores.
  - `service`: Lógica de negocio, servicios e interfaces de servicio.
  - `access`: Acceso a datos, persistencia JDBC SQLite y seguridad.
  - `domain`: Modelos de negocio puros, entidades inmutables y reglas de dominio.
  - `pipeline`: Tuberías y filtros para validación estructural de borradores.
  - `infra`: Infraestructura transversal desacoplada (Observer, Subject).
  - `main`: Punto de entrada (`MainApp`) que compone e inyecta dependencias manualmente.

### Iteración 2: Transición a Microservicios (Rama Actual: `segunda_iteracion`)
* **Enfoque:** Refactorización hacia una arquitectura distribuida orientada a microservicios con **Spring Boot 3.3.5**, comunicación HTTP/REST y mensajería orientada a eventos con **RabbitMQ**.
* **Patrón de Datos:** *Database per Service* (cada servicio gestiona de forma autónoma su propio almacenamiento).
* **Módulos y Servicios de la Solución:**
  1. **`auth-service` (Puerto 8081):** Microservicio para autenticación, gestión de credenciales (Argon2id) y emisión/validación de tokens de sesión firmados (HMAC-SHA256). Posee base de datos propia `auth.db`.
  2. **`validacion-service` (Puerto 8083):** Microservicio sin estado (*stateless* / in-memory) que expone el pipeline de 7 filtros para verificar la estructura y calidad de las preguntas.
  3. **`banco-service` (Puerto 8082):** Microservicio para administración del ciclo de vida de preguntas, persistencia en `banco.db`, consulta multicriterio, integración con `validacion-service` y publicación de eventos de auditoría y cambio de estado en RabbitMQ (`questions.exchange`).
  4. **Cliente de Escritorio (`src/`):** Aplicación JavaFX adaptada para comunicarse con `auth-service` a través de `RestAuthClient` y manejar sesiones desacopladas mediante tokens Bearer (`Session`).

---

## 3. Historias de Usuario y Funcionalidades

### HE1: Gestión de Usuarios (HU01)
* **Objetivo:** Registro, autenticación y control de usuarios para determinar quién usa el sistema y con qué privilegios.
* **Roles del Sistema:**
  - `ADMINISTRADOR`: Gestión global de usuarios y control total del banco de preguntas.
  - `AUTOR_PREGUNTAS`: Creación, edición de preguntas en borrador y envío a revisión.
  - `REVISOR`: Evaluación, retroalimentación, aprobación o rechazo de preguntas en revisión.
  - `DOCENTE` y `ESTUDIANTE`: Consulta de preguntas publicadas e información académica.
* **Seguridad y Criptografía:**
  - Almacenamiento seguro mediante algoritmo de derivación de claves **Argon2id** (`argon2-jvm`), descartando algoritmos rápidos vulnerables como MD5 o SHA-1.
  - **Política de contraseñas (`PasswordPolicy`):** Longitud mínima de 6 caracteres, al menos un dígito, al menos una letra mayúscula y al menos un carácter especial (`!@#$%^&*()_+-=[]{}|;:,.<>?`).
  - Nombre de usuario (`login`) único en el sistema.

### HE2: Gestión del Banco de Preguntas (HU02)
* **Objetivo:** Creación, edición y búsqueda estructurada de preguntas tipo Saber Pro.
* **Componentes de una Pregunta (`Question` / `QuestionDraft`):**
  - **Identificador:** Consecutivo estandarizado (`P-001`, `P-002`, ...).
  - **Nombre / Título:** Identificador legible de la pregunta.
  - **Contexto:** Enunciado o caso de estudio motivador (mínimo 20 caracteres).
  - **Pregunta Directa:** Interrogante concreto terminado en `?` (RF-09).
  - **Opciones de Respuesta:** Exactamente 5 opciones (4 distractores y 1 respuesta correcta).
  - **Metadatos Pedagógicos:** Competencia evaluada, Tema, Subtema, Nivel de dificultad (`BAJA`, `MEDIA`, `ALTA`).
  - **Soporte Académico:** Justificación explicativa y bibliografía de respaldo.
* **Búsqueda Multicriterio (`QuestionCriteria`):** Filtrado simultáneo o individual por estado, nivel de dificultad, competencia, tema, subtema y coincidencia de texto.
* **Regla de Inmutabilidad:** Solo se permite modificar preguntas que permanezcan en estado `BORRADOR`.

### HE3: Validación Estructural de Preguntas (HU03)
* **Objetivo:** Garantizar la consistencia pedagógica y formal de las preguntas antes de persistirlas.
* **Implementación:** Patrón arquitectónico **Pipeline (Tuberías y Filtros)**. Un borrador recorre una secuencia lineal de filtros; si una regla falla, el flujo se detiene inmediatamente y emite una `ValidationException` con los códigos y detalles del error.

| Orden | Filtro | Requisito | Regla de Validación |
| :---: | :--- | :---: | :--- |
| **1** | `ContextFilter` | RF-08 | Contexto presente y con un mínimo de 20 caracteres. |
| **2** | `DirectQuestionFilter` | RF-09 | Pregunta directa única, en formato de interrogación (termina en `?` y contiene solo un `?`). |
| **3** | `DistractorCountFilter` | RF-10 | Exactamente 4 distractores (5 opciones en total sumando la respuesta correcta). |
| **4** | `ForbiddenExpressionFilter` | RF-12 | Prohibición estricta de expresiones como *"Todas las anteriores"* y *"Ninguna de las anteriores"*. |
| **5** | `DistractorFormatFilter` | RF-13 | Opciones no vacías, de al menos 3 caracteres, con mayúscula inicial y sin repetirse entre sí. |
| **6** | `CorrectAnswerFilter` | RF-11 | Una única respuesta correcta con índice válido (entre 0 y 4), no duplicada en el conjunto. |
| **7** | `RequiredFieldsFilter` | RF-05 | Obligatoriedad de todos los campos: nombre, justificación, bibliografía, competencia, tema, subtema y dificultad. |

### HE4: Gestión del Ciclo de Vida y Auditoría (HU04)
* **Objetivo:** Administrar las fases por las que transita una pregunta, impidiendo inconsistencias de estado y registrando auditoría histórica.
* **Implementación:** Patrón **State (Máquina de Estados)** encapsulado en el enumerado `QuestionState`.
* **Regla de Integridad:** No existe borrado físico de preguntas en el banco; una pregunta rechazada o descartada pasa al estado `ARCHIVADA`.

#### Diagrama de Transición de Estados
```mermaid
stateDiagram-v2
    [*] --> BORRADOR
    BORRADOR --> PENDIENTE_REVISION: Enviar a revisión
    PENDIENTE_REVISION --> EN_REVISION: Iniciar revisión
    PENDIENTE_REVISION --> BORRADOR: Devolver a edición
    EN_REVISION --> APROBADA: Aprobar
    EN_REVISION --> RECHAZADA: Rechazar
    RECHAZADA --> BORRADOR: Reabrir para correcciones
    RECHAZADA --> ARCHIVADA: Archivar
    APROBADA --> PUBLICADA: Publicar para estudiantes
    APROBADA --> ARCHIVADA: Archivar
    PUBLICADA --> ARCHIVADA: Desactivar / Archivar
    ARCHIVADA --> [*]: Estado Final (Inmutable)
```

* **Auditoría de Estados (`QuestionStateChange`):** Cada transición exitosa se registra en la tabla `question_state_history` guardando:
  - `question_id`: Identificador de la pregunta.
  - `from_state`: Estado origen.
  - `to_state`: Estado destino.
  - `username`: Usuario que autorizó o ejecutó la acción.
  - `changed_at`: Marca temporal exacta (`LocalDateTime`).
* **Reactividad:** El cambio de estado dispara notificaciones automáticas mediante el patrón Observer para actualizar en tiempo real el contador y el gráfico circular de distribución de estados.

---

## 4. Patrones de Diseño de Software Aplicados

El proyecto destaca por su rigor en el uso de patrones de diseño GoF y principios de ingeniería de software:

```mermaid
classDiagram
    class Subject {
        +addObserver(Observer)
        +removeObserver(Observer)
        +notifyAllObservers(Object)
    }
    class Observer {
        <<interface>>
        +update(Object)
    }
    class IQuestionService {
        <<interface>>
        +createQuestion(QuestionDraft) Question
        +updateQuestion(String, QuestionDraft) Question
        +changeState(String, QuestionState, String) boolean
        +getStatistics() QuestionStatistics
    }
    class QuestionService {
        -IQuestionRepository repository
        -QuestionPipeline pipeline
    }
    class QuestionController {
        -IQuestionService service
    }
    class QuestionManagementView {
        +update(Object)
    }
    class QuestionChartView {
        +update(Object)
    }

    Subject <|-- QuestionService
    IQuestionService <|.. QuestionService
    Observer <|.. QuestionManagementView
    Observer <|.. QuestionChartView
    QuestionController --> IQuestionService
    Subject o-- Observer : notifica
```

1. **Inversión de Dependencias (DIP - SOLID):**
   - Las capas superiores no dependen de clases concretas de infraestructura, sino de contratos abstractos (`IUserService`, `IUserRepository`, `IQuestionService`, `IQuestionRepository`, `IAuthClient`, `IPasswordHasher`).
   - Facilita el cambio de persistencia (ej. de SQLite a PostgreSQL) o de transporte (ej. llamadas en memoria a clientes HTTP REST).
2. **Micro-patrón MVC (Model-View-Controller):**
   - **Modelo:** `QuestionService` (gestiona la lógica de negocio y las reglas del dominio).
   - **Controlador:** `QuestionController` (media entre los eventos de la vista y las operaciones del servicio).
   - **Vistas:** `QuestionManagementView`, `QuestionEditorView`, `QuestionStatisticsView`, `QuestionChartView`.
3. **Patrón Observer (Sujeto - Observador):**
   - `QuestionService` hereda de `Subject`.
   - Cuando se modifica una pregunta o cambia de estado, el servicio calcula las nuevas estadísticas (`QuestionStatistics`) y notifica automáticamente a todas las vistas observadoras registradas sin acoplarse a JavaFX.
4. **Patrón Pipeline (Tuberías y Filtros):**
   - `QuestionPipeline` encadena instancias que implementan la interfaz `Filter`.
   - Permite componer, alterar el orden o agregar nuevas validaciones fácilmente respetando el principio Abierto/Cerrado (OCP).
5. **Patrón State (Máquina de Estados):**
   - Cada constante del enumerado `QuestionState` implementa el método `allowedTransitions()`, delimitando de forma segura los saltos de estado válidos.
6. **Patrón Data Transfer Object (DTO):**
   - Objetos inmutables como `QuestionDraft`, `QuestionDraftRequest`, `ValidationResponse`, `RegisterRequest`, `LoginResponse` para desacoplar el transporte de datos de las entidades internas.

---

## 5. Arquitectura de Microservicios (Iteración 2)

En la segunda fase del proyecto, el sistema evoluciona hacia una arquitectura distribuida:

```mermaid
flowchart TD
    subgraph Frontend["Capa de Presentación"]
        JavaFX["Cliente Desktop JavaFX"]
        RestAuthClient["RestAuthClient (HTTP)"]
        JavaFX --> RestAuthClient
    end

    subgraph Microservicios["Ecosistema de Microservicios"]
        AuthSvc["auth-service (Puerto 8081)\n• Autenticación & Usuarios\n• Hashing Argon2id\n• Emisión de AuthTokens (HMAC-SHA256)"]
        ValSvc["validacion-service (Puerto 8083)\n• Stateless Pipeline\n• 7 Filtros de Calidad\n• POST /validaciones"]
        BancoSvc["banco-service (Puerto 8082)\n• Gestión de Preguntas & Estados\n• Consulta multicriterio\n• Verificación de Token"]
    end

    subgraph EventBus["Broker de Mensajería"]
        RabbitMQ[("RabbitMQ\nExchange: questions.exchange\nPuerto 5672")]
    end

    subgraph Storage["Persistencia Descentralizada"]
        AuthDB[("auth.db (SQLite)")]
        BancoDB[("banco.db (SQLite)")]
    end

    RestAuthClient -- "POST /login\nPOST /usuarios" --> AuthSvc
    AuthSvc --- AuthDB
    
    JavaFX -. "Consumo REST" .-> BancoSvc
    BancoSvc -- "POST /validaciones (Síncrono)" --> ValSvc
    BancoSvc --- BancoDB
    BancoSvc -- "Publicación de eventos (Asíncrono)" --> RabbitMQ
```

### Detalle de los Microservicios

#### A. `auth-service`
* **Tecnología:** Spring Boot 3.3.5, Java 17, SQLite JDBC, Argon2 JVM.
* **Puerto:** `8081`
* **Persistencia:** Base de datos independiente `auth.db`.
* **Endpoints:**
  - `POST /usuarios`: Registro de nuevos usuarios con contraseña cifrada en Argon2id.
  - `POST /login`: Validación de credenciales y generación de `AuthToken` firmado digitalmente (secreto compartido, validez de 60 minutos).
  - `GET /usuarios`: Listado de usuarios registrados (omite el hash de contraseña por seguridad).

#### B. `validacion-service`
* **Tecnología:** Spring Boot 3.3.5, Java 17.
* **Puerto:** `8083`
* **Persistencia:** No requiere (servicio puramente computacional y sin estado).
* **Endpoints:**
  - `POST /validaciones`: Valida borradores de preguntas contra los 7 filtros.
  - **Respuestas HTTP:**
    - `200 OK`: `{"valid": true, "errors": []}`
    - `422 Unprocessable Entity`: `{"valid": false, "errors": ["RF-xx: detalle del error"]}`
    - `400 Bad Request`: `{"valid": false, "errors": ["Solicitud mal formada"]}`
* **Herramientas:** Incluye colección exportada de Postman en `validacion-service/postman/validacion-service.postman_collection.json`.

#### C. `banco-service`
* **Tecnología:** Spring Boot 3.3.5, Spring AMQP (RabbitMQ), SQLite JDBC.
* **Puerto:** `8082`
* **Persistencia:** Base de datos independiente `banco.db` (tablas `questions`, `question_options`, `question_state_history`).
* **Integraciones:**
  - Valida tokens firmados emitidos por `auth-service` mediante `TokenVerifier`.
  - Invoca síncronamente vía HTTP a `validacion-service` en `http://localhost:8083` para validar cada borrador antes de guardarlo.
  - Publica eventos asíncronos en RabbitMQ (`questions.exchange` en `localhost:5672`) al crear preguntas o aplicar transiciones de ciclo de vida.
* **Endpoints:**
  - `GET /preguntas`: Consulta general y búsqueda multicriterio (`state`, `difficulty`, `competence`, `topic`, `subtopic`, `text`).
  - `GET /preguntas/{id}`: Consulta por identificador único.
  - `POST /preguntas`: Creación de preguntas en borrador (requiere rol `ADMINISTRADOR` o `AUTOR_PREGUNTAS`).
  - `PUT /preguntas/{id}`: Edición de preguntas en borrador (requiere rol `ADMINISTRADOR` o `AUTOR_PREGUNTAS`).
  - `PATCH /preguntas/{id}/estado`: Transición de la máquina de estados con registro de auditoría (requiere rol `ADMINISTRADOR`, `AUTOR_PREGUNTAS` o `REVISOR`).
  - `GET /preguntas/{id}/historial`: Consulta de la trazabilidad y auditoría de cambios de estado.
  - `GET /preguntas/estadisticas`: Conteo y distribución porcentual por estado.
* **Herramientas y Pruebas:**
  - Colección Postman en `banco-service/postman/banco-service.postman_collection.json`.
  - Batería de pruebas unitarias y de integración para repositorio, servicio, seguridad y controlador REST.

---

## 6. Modelo de Datos Relacional

### Esquema Inicial (Monolito) y Descentralizado
En la Iteración 1, las tablas residen en `taller2.db`. En la Iteración 2, `users` reside en `auth.db`, y el bloque de preguntas reside en `banco.db`.

```mermaid
erDiagram
    users {
        INTEGER id PK
        TEXT login UK "Login único"
        TEXT full_name "Nombre completo"
        TEXT role "ADMINISTRADOR, AUTOR_PREGUNTAS, etc."
        TEXT status "Activo / Inactivo"
        TEXT hashed_password "Hash Argon2id"
    }

    questions {
        TEXT id PK "Formato P-001"
        TEXT name "Nombre de la pregunta"
        TEXT statement "Enunciado / Pregunta directa"
        TEXT context "Contexto de la situación"
        INTEGER correct_index "Posición de la respuesta correcta"
        TEXT state "BORRADOR, APROBADA, etc."
        TEXT justification "Justificación pedagógica"
        TEXT bibliography "Bibliografía"
        TEXT competence "Competencia evaluada"
        TEXT topic "Tema principal"
        TEXT subtopic "Subtema"
        TEXT difficulty "BAJA, MEDIA, ALTA"
    }

    question_options {
        INTEGER id PK
        TEXT question_id FK "Referencia a questions"
        INTEGER position "Posición ordinal (0-4)"
        TEXT text "Texto de la opción"
    }

    question_state_history {
        INTEGER id PK
        TEXT question_id FK "Referencia a questions"
        TEXT from_state "Estado anterior"
        TEXT to_state "Nuevo estado"
        TEXT username "Usuario responsable"
        TEXT changed_at "Timestamp de la transición"
    }

    questions ||--o{ question_options : "tiene 5 opciones (1-N)"
    questions ||--o{ question_state_history : "registra historial (1-N)"
```

---

## 7. Estrategia de Pruebas y Aseguramiento de Calidad

El proyecto implementa una batería exhaustiva de pruebas automáticas con **JUnit 5** y **Mockito**:

1. **Dobles de Prueba Manuales (*Fakes*):**
   - `FakeUserRepository`, `FakeQuestionRepository`, `FakePasswordHasher`.
   - Permiten probar toda la lógica de negocio de los servicios de forma determinística y veloz en memoria sin necesidad de levantar bases de datos reales.
2. **Pruebas con Mockito:**
   - Pruebas sobre interfaces (`QuestionServiceMockitoTest`, `QuestionControllerMockitoTest`).
   - Uso de `ArgumentCaptor` para certificar que los datos de auditoría se capturan y envían fielmente a los repositorios.
3. **Pruebas de Repositorio sobre SQLite:**
   - Pruebas reales de persistencia usando conexiones SQLite en memoria (`:memory:`), validando esquemas, transacciones y consultas de historial.
4. **Pruebas de Pipeline y Filtros:**
   - Pruebas independientes para cada uno de los 7 filtros verificando tanto casos límite positivos como negativos, más pruebas completas de integración de punta a punta (`QuestionPipelineTest`).
5. **Pruebas de Integración y API REST:**
   - Pruebas de contrato HTTP con `MockMvc` en `auth-service` y `validacion-service`.
6. **Validación de Usabilidad (Thinking Aloud):**
   - Sesión experimental documentada con 3 estudiantes ajenos al equipo de desarrollo, evaluando los flujos de inicio de sesión, redacción con el editor de preguntas, búsqueda por filtros y cambio de estados, recopilando oportunidades de mejora ergonómica y visual.

---

## 8. Estructura del Repositorio

```
proyecto-saberpro-ing-software-II/
│
├── auth-service/                       # Microservicio de Autenticación (Spring Boot 3.3.5)
│   ├── src/main/java/                  # Controladores REST, TokenSigner, UserService, Argon2
│   ├── src/main/resources/             # application.properties (puerto 8081, auth.db)
│   ├── src/test/java/                  # Pruebas unitarias y de integración REST
│   └── pom.xml
│
├── banco-service/                      # Microservicio de Banco de Preguntas (Spring Boot 3.3.5)
│   ├── src/main/java/                  # Modelos de dominio Question, State, RabbitMQ config
│   ├── src/main/resources/             # application.properties (puerto 8082, banco.db, RabbitMQ)
│   └── pom.xml
│
├── validacion-service/                 # Microservicio de Validación Estructural (Spring Boot 3.3.5)
│   ├── postman/                        # Colección Postman de prueba
│   ├── src/main/java/                  # Pipeline, los 7 Filtros, ValidationController
│   ├── src/main/resources/             # application.properties (puerto 8083)
│   ├── src/test/java/                  # 44 pruebas unitarias del pipeline y controlador
│   ├── README.md                       # Especificación técnica del servicio
│   └── pom.xml
│
├── docs/                               # Documentación de Diseño y Arquitectura
│   ├── Decisiones_Tecnicas_...pdf      # Justificación técnica de alternativas elegidas y descartadas
│   ├── Documento_Arquitectura_...pdf   # Documento C4, Modelo 4+1, Usabilidad, Pruebas
│   └── MiniQAW_...xlsx                 # Atributos de calidad y escenarios QAW
│
├── src/                                # Monolito Inicial y Cliente Desktop JavaFX
│   ├── main/java/com/unicauca/lisw2t2g08/
│   │   ├── access/                     # SQLiteQuestionRepository, SQLiteUserRepository, RestAuthClient
│   │   ├── domain/                     # User, Question, Session, PasswordPolicy, QuestionState
│   │   ├── infra/                      # Subject, Observer (Patrón Observer)
│   │   ├── main/                       # MainApp (ensamblador e inyector de dependencias)
│   │   ├── pipeline/                   # Pipeline y filtros (monolito)
│   │   ├── service/                    # QuestionService, UserService, interfaces
│   │   └── ui/                         # Vistas JavaFX y QuestionController (MVC)
│   └── test/java/                      # 152 pruebas automatizadas del monolito
│
├── pom.xml                             # Configuración Maven principal del cliente/monolito
└── README.md                           # Guía general de ejecución y resumen del proyecto
```

---

## 9. Instrucciones de Compilación y Ejecución

### Prerrequisitos
* **Java Development Kit (JDK):** Versión 17 o superior.
* **Apache Maven:** Versión 3.8 o superior.
* **RabbitMQ** (para mensajería en `banco-service`): Corriendo en `localhost:5672`.

### 1. Ejecutar el Monolito / Cliente Gráfico (JavaFX)
Desde la raíz del repositorio:
```bash
mvn clean compile
mvn javafx:run
```

### 2. Ejecutar `auth-service`
```bash
cd auth-service
mvn spring-boot:run
```
*Disponible en:* `http://localhost:8081`

### 3. Ejecutar `validacion-service`
```bash
cd validacion-service
mvn spring-boot:run
```
*Disponible en:* `http://localhost:8083`

### 4. Ejecutar Pruebas Unitarias
Para correr la suite de pruebas en cualquier módulo:
```bash
mvn test
```
