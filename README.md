# Proyecto Banco de Preguntas Pruebas Saber Pro

Aplicación de escritorio en Java (JavaFX + SQLite) construida siguiendo los
principios SOLID, con Inversión de Dependencias como eje central

## Historia de usuario implementada

**HE 1: Gestión de usuarios**

| Funcionalidad | Descripción |
|---|---|
| Registro de usuarios | Login, nombre completo, rol, estado (Activo/Inactivo) y contraseña. El login es único. |
| Inicio de sesión | Verificación de credenciales contra la contraseña cifrada almacenada. |
| Listado de usuarios | Consulta de los usuarios registrados. |
| Política de contraseña | Mínimo 6 caracteres, al menos un dígito, una mayúscula y un carácter especial. |
| Cifrado | La contraseña se guarda con Argon2id, nunca en texto plano. |

**Roles disponibles:** `ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE`, `ESTUDIANTE`.

**Banco de preguntas: gestión del estado** (patrón Capas + micro patrón MVC + patrón Observer)

Desde el menú principal, los roles `ADMINISTRADOR`, `AUTOR_PREGUNTAS` y `REVISOR`
abren el *Banco de preguntas*: se selecciona una pregunta, se cargan sus datos
(Id, Nombre, Pregunta, Opciones, Respuesta correcta, Estado) y se cambia su estado
entre *Borrador*, *Pendiente de revisión* y *Eliminada* (una pregunta eliminada no
puede reactivarse). Cada cambio notifica automáticamente a dos vistas
observadoras: el conteo de preguntas por estado y una gráfica en pastel con los
porcentajes.

| Rol MVC | Clase |
|---|---|
| Modelo (y sujeto observable) | `QuestionService` (extiende `Subject`, implementa `IQuestionService`) |
| Vista | `QuestionManagementView`, `QuestionStatisticsView`, `QuestionChartView` |
| Controlador | `QuestionController` |

## Arquitectura

```
src/main/java/com/unicauca/lisw2t2g08/
├── main/      MainApp (punto de entrada y composición de dependencias)
├── ui/        LoginView, RegisterView, MainMenuView (JavaFX)
│              QuestionController, QuestionManagementView, QuestionStatisticsView,
│              QuestionChartView, QuestionBankWindows
├── service/   IUserService, UserService y excepciones de negocio
│              IQuestionService, QuestionService
├── access/    IUserRepository, SQLiteUserRepository, DatabaseConnection, Argon2PasswordHasher
│              IQuestionRepository, SQLiteQuestionRepository, QuestionSampleData
├── domain/    User, Role, UserStatus, PasswordPolicy, IPasswordHasher
│              Question, QuestionDistractors, QuestionState, QuestionStatistics
└── infra/     Observer, Subject (patrón Observer)
```

Las capas superiores dependen de abstracciones (`IUserService`,
`IUserRepository`, `IPasswordHasher`, `IQuestionService`, `IQuestionRepository`),
y las implementaciones concretas se inyectan en `MainApp`.

## Requisitos

- JDK compatible con el `pom.xml`
- Maven

## Cómo ejecutar

```bash
mvn clean compile
mvn javafx:run
```

Se crea (o reutiliza) el archivo SQLite `taller2.db` en la raíz del proyecto
(ignorado por git). La primera vez se precargan ocho preguntas de ejemplo en las
tablas `questions` y `question_options`.

## Pruebas unitarias

```bash
mvn test
```

Cubren `User`, `PasswordPolicy` y `UserService` (con repositorio y hasher
falsos), y en el banco de preguntas: `Question`, `QuestionDistractors`,
`QuestionStatistics`, `Subject`, `QuestionService` (con `FakeQuestionRepository`
y notificación a los observadores), `QuestionController` (con un servicio falso)
y `SQLiteQuestionRepository` (sobre SQLite en memoria).

