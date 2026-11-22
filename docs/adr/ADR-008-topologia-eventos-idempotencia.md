# ADR-008: Topología de eventos e idempotencia por eventId

- **Estado:** Aceptada; entrega fiable completa pendiente
- **Fecha:** 2026-11-19 (historial Git: `b6c474a` Auditoría, `ed520ea` Revisión)

## Contexto

Banco publica creación y cambios de estado; Auditoría y Revisión requieren proyecciones diferentes. La topología se describe en `docs/decisiones-iteracion-2.md`, §4, y D-004/D-007 del SAD (`docs_2/SAD.pdf`, §8).

## Decisión

Usar exchange topic durable `saberpro.events`. Claves: `question.created` y `question.state-changed`. Colas durables: `auditoria.preguntas` (binding `question.*`) y `revision.preguntas` (binding `question.state-changed`). Cada evento contiene `eventId`; los consumidores persisten IDs procesados para deduplicar.

## Alternativas consideradas

El SAD y las decisiones documentan mensajería con eventos frente a integración síncrona y consumidores separados. No se agregan opciones no incluidas en las fuentes.

## Consecuencias

Auditoría y Revisión pueden procesar de forma asíncrona y deduplicar. La publicación no es transaccional con la base de Banco (sin Outbox). Los consumidores reintentan 3 veces, pero no tienen DLQ declarada; un mensaje fallido puede descartarse tras agotar intentos. La consistencia entre Revisión y Banco no es atómica.
