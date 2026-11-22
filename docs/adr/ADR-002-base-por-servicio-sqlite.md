# ADR-002: Base propia por servicio con SQLite

- **Estado:** Aceptada
- **Fecha:** 2026-11-19 (historial Git: integración de configuración independiente de los cuatro servicios, `e5668ce`)

## Contexto

Cada servicio debe ser dueño de sus datos. El SAD registra D-008: SQLite y Database per Service para la fase académica, con limitación de escalado (`docs_2/SAD.pdf`, §8). `docs/decisiones-iteracion-2.md`, §6, documenta conexión compartida y métodos sincronizados.

## Decisión

Usar una base SQLite por servicio: `auth.db`, `banco.db`, `revision.db`, `auditoria.db`. Las configuraciones están en los `application.properties` correspondientes.

## Alternativas consideradas

El SAD menciona MariaDB/Postgres como expectativa a validar frente al enunciado; el documento de decisiones iniciales presenta SQLite para la aplicación monolítica (`docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf`, sección Plataforma). No se documentan otras alternativas en las fuentes consultadas.

## Consecuencias

Los datos se aíslan y las referencias entre servicios son por identificador sin FK. SQLite, conexión compartida y métodos `synchronized` limitan el uso con múltiples instancias; no hay pruebas de carga ni escalado demostrable. La aceptación académica de esta limitación queda pendiente de validación del profesor.
