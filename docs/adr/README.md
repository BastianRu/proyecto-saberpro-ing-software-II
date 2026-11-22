# Índice de Architecture Decision Records

Fechas derivadas del historial Git: cambios de implementación de la arquitectura final del 2026-11-19 (`e5668ce` y commits asociados); cliente JavaFX integrado el 2026-11-18 (`f70ec09`); SAD de referencia actualizado el 2026-11-20 (`feac29b`). Las decisiones iniciales se contrastan con `docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf`.

| ADR | Decisión | Estado | Fecha |
|---|---|---|---|
| [ADR-001](ADR-001-microservicios-eventos.md) | Microservicios y eventos | Aceptada | 2026-11-19 |
| [ADR-002](ADR-002-base-por-servicio-sqlite.md) | Base de datos propia por servicio con SQLite | Aceptada | 2026-11-19 |
| [ADR-003](ADR-003-token-firmado-compartido.md) | Token HMAC firmado y validación local | Aceptada | 2026-11-18 |
| [ADR-004](ADR-004-validacion-embebida.md) | Validación embebida en Banco | Aceptada | 2026-11-19 |
| [ADR-005](ADR-005-servicio-revision.md) | Revisión por pares ocupa el cuarto servicio | Aceptada | 2026-11-19 |
| [ADR-006](ADR-006-state-jerarquia.md) | State como jerarquía de clases | Aceptada | 2026-11-19 |
| [ADR-007](ADR-007-pipeline-filtros.md) | Pipeline de filtros | Aceptada | 2026-11-19 |
| [ADR-008](ADR-008-topologia-eventos-idempotencia.md) | Topología topic e idempotencia | Aceptada | 2026-11-19 |
| [ADR-009](ADR-009-cliente-escritorio-pendiente.md) | Cliente de escritorio aún local | Aceptada (limitación documentada) | 2026-11-18 |

> El SAD `entregables/sprint-2/SAD_Banco_Preguntas_Iteracion2.pdf`, §8, agrupa D-001 a D-008, pero indica que no existían ADR individuales. Estos archivos formalizan las decisiones ya documentadas; no añaden alternativas nuevas. Ver también `docs/decisiones-iteracion-2.md`.
