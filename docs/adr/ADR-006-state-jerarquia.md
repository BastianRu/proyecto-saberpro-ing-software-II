# ADR-006: Ciclo de vida con jerarquía State

- **Estado:** Aceptada
- **Fecha:** 2026-11-19 (historial Git: `3832389`, jerarquía de estados en Banco)

## Contexto

La primera versión del Banco describía `QuestionState` como enumeración. El código actual implementa el patrón State con clases concretas; el cambio se registra en `docs/decisiones-iteracion-2.md`, §2, y en D-005 del SAD (`docs_2/SAD.pdf`, §8).

## Decisión

Representar cada estado como subclase de `QuestionState`, que valida operaciones mediante polimorfismo y doble despacho. Las instancias sin estado mutable se comparten desde `QuestionStates`.

## Alternativas consideradas

Enum con transiciones permitidas (diseño anterior) frente a jerarquía de clases (diseño actual), según `docs/decisiones-iteracion-2.md`, §2.

## Consecuencias

Las operaciones válidas quedan localizadas en cada estado. Se incrementa la cantidad de clases; las transiciones se verifican en `banco-service/src/test/java/.../QuestionStateTest.java`.
