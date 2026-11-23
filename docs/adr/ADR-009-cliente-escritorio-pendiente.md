# ADR-009: Mantener temporalmente el cliente de escritorio de primera iteración

- **Estado:** Aceptada como limitación temporal; migración pendiente
- **Fecha:** 2026-11-18 (historial Git: `f70ec09`, integración de Auth en cliente JavaFX)

## Contexto

El repositorio conserva `src/` como aplicación JavaFX de la iteración 1. En la migración, la autenticación se conectó a `auth-service`, pero las operaciones de preguntas siguen usando repositorio local. La limitación se documenta en `docs/decisiones-iteracion-2.md`, §6 y `CONTEXTO_PROYECTO.md`, §2.

## Decisión

Conservar el cliente y conectar su inicio de sesión a `auth-service`; no afirmar que el cliente consume el Banco remoto. Evidencia: `src/main/java/com/unicauca/lisw2t2g08/main/MainApp.java` crea `RestAuthClient` y también instancia `SQLiteQuestionRepository` local.

## Alternativas consideradas

Mantener el cliente anterior frente a migrar el cliente a endpoints de los servicios. Las fuentes confirman que solo se implementó la conexión de autenticación y que la migración del Banco queda pendiente; no se detallan otras alternativas.

## Consecuencias

La refactorización del backend no completa la migración del flujo de escritorio. Esta brecha se registra como parcial en `docs/CUMPLIMIENTO_SPRINT2.md` y requiere decisión del equipo sobre alcance.
