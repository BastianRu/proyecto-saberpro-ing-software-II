# Taller 2 - Principios SOLID - Gestión de usuarios

Aplicación de escritorio en Java (JavaFX + SQLite) para el registro e inicio
de sesión de usuarios del sistema, construida siguiendo los principios SOLID
con Inversión de Dependencias como eje central (basada en el "ejemplo 5"
visto en la clase teórica de Laboratorio de Ingeniería del Software II).

## Cómo ejecutar

```bash
mvn clean compile
mvn javafx:run
```

Esto crea (o reutiliza) un archivo `usuarios.db` de SQLite en la raíz del
proyecto. Si prefieres una base de datos en memoria (se borra al cerrar la
app), cambia en `MainApp.java`:

```java
IConnectionProvider connectionProvider = SQLiteConnectionProvider.file("usuarios.db");
// por:
IConnectionProvider connectionProvider = SQLiteConnectionProvider.inMemory();
```

## Cómo correr las pruebas unitarias

```bash
mvn test
```

## Registro y validación de contraseña

Al registrar un usuario se piden: login, nombre completo, rol, estado
(Activo/Inactivo) y contraseña. La contraseña debe tener mínimo 6 caracteres,
al menos un dígito, al menos una mayúscula y al menos un carácter especial.
Se guarda cifrada con Argon2id (nunca en texto plano).
