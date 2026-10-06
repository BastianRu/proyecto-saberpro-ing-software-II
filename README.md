# Taller 2 - Principios SOLID

Aplicación de escritorio en Java (JavaFX + SQLite) construida siguiendo los
principios SOLID, con Inversión de Dependencias como eje central (basada en el
"ejemplo 5" visto en la clase teórica de Laboratorio de Ingeniería del
Software II).

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

## Arquitectura

```
src/main/java/com/unicauca/lisw2t2g08/
├── main/      MainApp (punto de entrada y composición de dependencias)
├── ui/        LoginView, RegisterView, MainMenuView (JavaFX)
├── service/   IUserService, UserService y excepciones de negocio
├── access/    IUserRepository, SQLiteUserRepository, DatabaseConnection, Argon2PasswordHasher
└── domain/    User, Role, UserStatus, PasswordPolicy, IPasswordHasher
```

Las capas superiores dependen de abstracciones (`IUserService`,
`IUserRepository`, `IPasswordHasher`), y las implementaciones concretas se
inyectan en `MainApp`.

## Requisitos

- JDK compatible con el `pom.xml`
- Maven

## Cómo ejecutar

```bash
mvn clean compile
mvn javafx:run
```

Se crea (o reutiliza) el archivo SQLite `taller2.db` en la raíz del proyecto
(ignorado por git).

## Pruebas unitarias

```bash
mvn test
```

Cubren `User`, `PasswordPolicy` y `UserService` (con repositorio y hasher
falsos).

## Diagramas

Los diagramas del modelo C4 se encuentran en la carpeta `C4_MODEL/`.
