# Información redundante entre servicios

> Verificado contra el código de la rama `integracion-iteracion2` el
> 2026-11-18. Cada afirmación de este documento se comprobó leyendo los archivos
> citados y comparándolos con `diff`/`grep`; no se refactorizó nada.

## Criterio

El proyecto prohíbe compartir clases mediante una librería común: cada servicio
conserva su copia mínima de los contratos. Por eso hay tipos repetidos. La
pregunta no es «¿se repiten?» (sí), sino «¿la repetición es un contrato
necesario o una duplicación que conviene reducir?».

## Tabla

| Elemento | Dónde está | Por qué existe | ¿Es aceptable? | Acción recomendada |
|---|---|---|---|---|
| `Role` | `auth-service/.../auth/domain/Role.java`, `banco-service/.../banco/domain/Role.java`, `src/main/java/.../domain/Role.java` | `auth-service` lo usa para registrar y firmar el rol dentro del token; `banco-service` lo usa para autorizar operaciones; `src/` es la copia del monolito de la iteración 1. Las constantes son las mismas (`ADMINISTRADOR`, `AUTOR_PREGUNTAS`, `REVISOR`, `DOCENTE`, `ESTUDIANTE`); solo cambian el paquete y el formato. | **Sí, como contrato de cable.** Es un enum que viaja dentro del token y de las peticiones. Unificarlo con una librería acoplaría los servicios. | Mantener los cinco valores sincronizados. Si se agrega un rol, actualizar auth, banco y la documentación en el mismo cambio. |
| `AuthToken` | `auth-service/.../auth/domain/AuthToken.java`, `banco-service/.../banco/domain/AuthToken.java` | `auth-service` lo construye al emitir el token; `banco-service` lo construye al verificarlo. Tiene los mismos cuatro campos (`subject`, `role`, `expiresAt`, `signature`) y la misma regla `isExpired`. | **Sí.** Es un DTO de contrato interno de cada servicio; compartirlo exigiría una librería común. | Mantener el orden y el significado de los campos. El formato exacto está cubierto por la prueba de vector fijo en ambos servicios. |
| `DifficultyLevel` | `banco-service/.../banco/domain/DifficultyLevel.java`, `validacion-service/.../validacion/domain/DifficultyLevel.java`, `src/main/java/.../domain/DifficultyLevel.java` | `banco-service` lo persiste y lo expone; `validacion-service` lo recibe en `POST /validaciones`; `src/` es la versión del monolito. Los valores (`BAJA`, `MEDIA`, `ALTA`) y las etiquetas coinciden; solo cambian el paquete y los comentarios. | **Sí, como contrato JSON.** El nombre del enum es parte del cuerpo que viaja entre servicios. | Mantener los tres valores. Un valor nuevo debe añadirse en ambos servicios y en el pipeline antes de aceptarlo. |
| `QuestionDraft` | `banco-service/.../banco/domain/QuestionDraft.java`, `validacion-service/.../validacion/domain/QuestionDraft.java`, `src/main/java/.../domain/QuestionDraft.java` | `banco-service` lo arma desde `QuestionRequest` y lo envía a validación; `validacion-service` lo recibe y lo recorre con el pipeline; `src/` es la versión del monolito. Las tres copias tienen los mismos once campos y los mismos accesores (verificado); los paquetes y algunos comentarios cambian. | **Sí, pero es la duplicación más delicada.** Cualquier campo nuevo debe viajar en el JSON de `POST /validaciones`; si solo se agrega en un lado, el pipeline no lo verá. | Documentar el contrato en `RULES.md` y en los README. Si se agrega un campo, actualizar `QuestionDraft` de banco, `QuestionDraft`/`QuestionDraftRequest` de validación y `RestValidationClient`. |
| `ValidationException` | `banco-service/.../banco/client/ValidationException.java`, `validacion-service/.../validacion/pipeline/ValidationException.java`, `src/main/java/.../pipeline/ValidationException.java` | En validación y en el monolito la lanza un filtro cuando una regla falla. En banco es la excepción con la que el cliente REST traduce un `422` remoto. No son la misma clase ni tienen el mismo origen: banco la usa para decidir el `422` local; validación la lanza desde el pipeline. | **Parcialmente.** La duplicación de nombre confunde, aunque la semántica de «reglas incumplidas» es la misma. | No unificarlas (sería una librería común). Si se toca el código, valorar renombrar la de banco a `RemoteValidationException`; no se hizo para no cambiar contratos internos sin necesidad. |
| Secreto del token | `auth-service/src/main/resources/application.properties` (`auth.token.secret`) y `banco-service/src/main/resources/application.properties` (`banco.token.secret`) | `auth-service` firma con HMAC-SHA256 y `banco-service` verifica con el mismo secreto. Ambos archivos tienen hoy el mismo valor de desarrollo (`dev-secret-cambiar-en-produccion-0123456789`). | **Sí, pero es configuración operativa, no código.** No se puede eliminar sin un servicio de configuración o un secreto compartido externo, que el proyecto no usa. | En producción, inyectar el mismo secreto por variables de entorno en ambos servicios y no dejar el valor por defecto. Está anotado en `docs/PENDIENTES.md`. |
| Código del monolito en `src/` | `src/main/java/com/unicauca/lisw2t2g08/` | Es la implementación de la iteración 1 (JavaFX + SQLite): `User`, `Role`, `PasswordPolicy`, `Question`, `QuestionState`, `QuestionDraft`, el pipeline de filtros y las vistas. Los servicios de la iteración 2 no lo importan (verificado con `grep`). | **Sí, como legado histórico.** No participa en la ejecución de los microservicios. Borrarlo o moverlo no forma parte de este trabajo y podría romper la entrega de la iteración 1. | No tocarlo. Está marcado como versión anterior en el `README.md` raíz. |

## Observaciones

1. **No hay dependencia de código entre servicios.** `banco-service` no importa
   paquetes de `auth-service` ni de `validacion-service`; solo comparte formato
   de token y cuerpos JSON. `auth-service` no importa nada de los otros.
2. **La duplicación de `Role`, `AuthToken`, `DifficultyLevel` y `QuestionDraft`
   es el precio de no tener una librería común.** Es aceptable mientras los
   contratos estén documentados y probados. La prueba de vector fijo del token
   cubre el caso más sensible.
3. **`src/` no es una dependencia de los servicios.** Es la versión de la
   primera iteración; se conserva por trazabilidad académica.
