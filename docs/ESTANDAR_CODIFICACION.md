# Estándar de codificación observado

> Este documento describe convenciones existentes en el código; no impone estilos nuevos ni configura herramientas. La muestra cubre `auth-service`, `banco-service`, `revision-service`, `auditoria-service` y el monolito `src/`. Si una convención no es uniforme, se anota en excepciones.

## Estructura de paquetes

| Convención observada | Ejemplo real |
|---|---|
| Paquetes con raíz institucional `com.unicauca.lisw2t2g08` y subpaquete del servicio. | `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/` |
| Separación por responsabilidades `api`, `service`, `domain`, `access`, `security`, `events`, `config`. | `revision-service/src/main/java/com/unicauca/lisw2t2g08/revision/` |
| Clases API contienen controladores, DTO y manejadores HTTP. | `auth-service/.../auth/api/AuthController.java`, `ApiExceptionHandler.java` |
| Persistencia se separa de la lógica de negocio mediante repositorios. | `banco-service/.../banco/access/IQuestionRepository.java`, `SQLiteQuestionRepository.java` |

## Nombres

- Clases e interfaces usan `UpperCamelCase`, por ejemplo `QuestionController`, `QuestionService` y `QuestionStateChangedEvent` (`banco-service/.../api/QuestionController.java` y `.../events/QuestionStateChangedEvent.java`).
- Métodos y campos usan `lowerCamelCase`, por ejemplo `createQuestion`, `questionId` y `occurredAt` (`banco-service/.../service/QuestionService.java`).
- Interfaces de repositorio y puertos/adaptadores suelen llevar prefijo `I`: `IQuestionRepository`, `IEventPublisher`, `IBancoClient` (`banco-service/.../access/IQuestionRepository.java`, `.../events/IEventPublisher.java`, `revision-service/.../service/IBancoClient.java`).
- Interfaces de servicio también usan `I`: `IUserService` en `auth-service/.../service/IUserService.java`.
- Pruebas se nombran con sufijo `Test` y el objeto probado: `QuestionPipelineTest.java`, `ReviewServiceTest.java`, `AuditServiceTest.java`.
- Los identificadores de código, nombres de propiedades y claves de eventos están en inglés. Comentarios Java existen tanto en español como en inglés según módulo/archivo; no se adopta una regla de idioma para comentarios porque no es uniforme.

## Capas e inversión de dependencias

- Las API delegan a servicios; por ejemplo `QuestionController` recibe `IQuestionService` (`banco-service/.../api/QuestionController.java`).
- La lógica depende de interfaces para persistencia y eventos: `QuestionService` recibe `IQuestionRepository` e `IEventPublisher` (`banco-service/.../service/QuestionService.java`).
- La configuración compone implementaciones concretas, por ejemplo `BancoConfig` crea `SQLiteQuestionRepository` y `RabbitEventPublisher` (`banco-service/.../config/BancoConfig.java`).
- Revisión consume Banco a través de `IBancoClient`; la implementación REST se crea en `revision-service/.../config/ReviewConfig.java`.

## DTO y respuestas

Los servicios usan `record` para DTO compactos de API y eventos. Ejemplos: `QuestionController.QuestionResponse`, `QuestionCreatedEvent`, `QuestionStateChangedEvent` (`banco-service/.../api/QuestionController.java`, `.../events/QuestionCreatedEvent.java`, `.../events/QuestionStateChangedEvent.java`). Esta convención es habitual, no universal.

## Errores HTTP

Los servicios mantienen un `ApiExceptionHandler` y convierten excepciones de dominio/API en respuestas HTTP. Ejemplos: `banco-service/.../api/ApiExceptionHandler.java` mapea validación a 422 y errores de autorización; `auth-service/.../api/ApiExceptionHandler.java` cubre usuario duplicado, validación y autenticación. El formato de cuerpos no es idéntico entre servicios.

## Configuración y eventos

- Propiedades en `src/main/resources/application.properties` usan nombres jerárquicos con punto: `server.port`, `auth.token.secret`, `banco.db.url`, `revision.banco.url`, `auditoria.db.url`.
- El nombre de aplicación coincide con el módulo en las propiedades (`spring.application.name=revision-service`, etc.).
- Tipos de eventos y campos usan nombres en inglés: `QuestionCreatedEvent`, `QuestionStateChangedEvent`, `eventId`, `questionId`, `occurredAt` (`banco-service/.../events/`).
- Claves de routing en minúsculas con puntos: `question.created`, `question.state-changed`; exchange `saberpro.events` (`banco-service/.../events/RabbitEventPublisher.java`, consumidores `*/config/AmqpConfig.java`).

## Excepciones y puntos no uniformes

1. **Prefijo `I`:** el código tiene interfaces sin prefijo `I`, por ejemplo `Filter` y `QuestionState` (abstract class) en Banco, y algunos puertos bajo nombres no uniformes. No convertir la observación en requisito absoluto.
2. **`record`:** algunos DTO son records y otros DTO/entidades son clases, como `QuestionDraft` y `AuthToken`.
3. **Idioma de comentarios:** hay comentarios en español, inglés y sin tildes; los identificadores y contratos siguen en inglés.
4. **Capas:** el cliente JavaFX usa paquetes `ui`, `infra` y `main`, no las capas de microservicios; `revision-service` ubica `IBancoClient` en `service`.
5. **Errores HTTP:** los cuerpos varían entre `message/errors`, `error/details` y respuestas de validación específicas. Comparar el handler del servicio antes de reutilizar un cliente.
6. **Pruebas:** los sufijos `Test` son consistentes en los servicios, pero no se impone sufijo `Tests` u otra norma del proyecto.
7. **Reglas de formato automatizadas:** no se encontró configuración Checkstyle o Spotless. Adoptarlos sería una propuesta para el equipo, no una convención actual.
8. **Puntos de configuración de token:** Auth usa `auth.token.secret`; los otros servicios usan también `auth.token.secret` en la iteración actual (contrastar las propiedades, no asumir prefijos diferentes de documentos previos).

## Alcance

Este estándar no prescribe algoritmos, arquitectura nueva ni cambios de código. Se basa en archivos existentes y se debe revisar con los cambios futuros.
