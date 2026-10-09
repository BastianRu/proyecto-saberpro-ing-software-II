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

**HE 3: Validación estructural** (patrón Pipeline / Tuberías y Filtros)

Un borrador de pregunta (`QuestionDraft`) atraviesa una tubería de filtros en
orden; cada filtro valida una regla y, si falla, la tubería se detiene y lanza
una `ValidationException` con los errores encontrados.

| Orden | Filtro | Requisito | Regla |
|---|---|---|---|
| 1 | `ContextFilter` | RF-08 | Toda pregunta debe tener contexto (mínimo 20 caracteres). |
| 2 | `DirectQuestionFilter` | RF-09 | Una única pregunta directa, con formato de pregunta (termina en `?`, un solo `?`). |
| 3 | `DistractorCountFilter` | RF-10 | Exactamente cuatro distractores (cinco opciones con la correcta). |
| 4 | `ForbiddenExpressionFilter` | RF-12 | Se prohíben "Todas las anteriores" y "Ninguna de las anteriores". |
| 5 | `DistractorFormatFilter` | RF-13 | Opciones no vacías, de al menos 3 caracteres, con mayúscula inicial y sin repetirse. |
| 6 | `CorrectAnswerFilter` | RF-11 | Una única respuesta correcta, con índice válido y no repetida entre las opciones. |

```java
QuestionPipeline pipeline = new QuestionPipeline();
try {
    pipeline.run(draft);              // borrador válido: lo devuelve
} catch (ValidationException e) {
    e.getErrors();                    // lista de mensajes "RF-xx: ..."
}
```

`QuestionPipeline` también acepta una lista de filtros propia
(`new QuestionPipeline(List<Filter>)`) para componer tuberías a la medida.
Por ahora el pipeline es una capa independiente: no está conectado a la interfaz.

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
│              QuestionDraft (borrador que recorre el pipeline)
├── pipeline/  Filter, QuestionPipeline, ValidationException (patrón Pipeline, HE 3)
│              ContextFilter, DirectQuestionFilter, DistractorCountFilter,
│              ForbiddenExpressionFilter, DistractorFormatFilter, CorrectAnswerFilter
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

Para HE 3 hay una clase de prueba por filtro (`ContextFilterTest`,
`DirectQuestionFilterTest`, `DistractorCountFilterTest`,
`ForbiddenExpressionFilterTest`, `DistractorFormatFilterTest`,
`CorrectAnswerFilterTest`) y `QuestionPipelineTest`, que valida la tubería de
punta a punta.

