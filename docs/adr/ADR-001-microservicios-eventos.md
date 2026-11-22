# ADR-001: Microservicios orientados a eventos

- **Estado:** Aceptada
- **Fecha:** 2026-11-19 (historial Git: `e5668ce`, integración de microservicios y orientación a eventos)

## Contexto

La iteración 2 separa autenticación, preguntas, revisión y auditoría en módulos independientes. El SAD recoge esta decisión en D-001 y D-004 (`docs_2/SAD.pdf`, §8); la descripción de los contratos está en `docs/decisiones-iteracion-2.md`, §§2 y 4.

## Decisión

Mantener cuatro servicios Spring Boot independientes: `auth-service`, `banco-service`, `revision-service` y `auditoria-service`. Las llamadas de operación entre Banco y Revisión usan REST; Banco publica eventos por RabbitMQ para consumidores desacoplados.

## Alternativas consideradas

El documento inicial de decisiones compara monolito en capas, arquitectura de microservicios y acceso directo de la GUI a SQLite (`docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf`, sección de estilo arquitectónico). La decisión final adopta microservicios según `docs/decisiones-iteracion-2.md`.

## Consecuencias

Hay límites de responsabilidad y despliegue por módulo, pero se requiere coordinación de contratos, operación del broker y manejo de consistencia eventual. La escalabilidad operativa no se considera demostrada (ver `docs/CUMPLIMIENTO_SPRINT2.md`).
