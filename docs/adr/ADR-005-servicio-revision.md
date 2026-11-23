# ADR-005: Servicio de revisión por pares como cuarto servicio

- **Estado:** Aceptada
- **Fecha:** 2026-11-19 (historial Git: `ed520ea`, incorporación de `revision-service`)

## Contexto

El equipo aplicó la sugerencia del profesor de tener un servicio de REVISOR «a cambio de otro». En el diseño inicial, el cuarto módulo era validación; en la versión final validación se embebió y Revisión ocupa su lugar. Fuentes: `docs/decisiones-iteracion-2.md`, §§1–3; SAD D-006, `entregables/sprint-2/SAD_Banco_Preguntas_Iteracion2.pdf`, §8.

## Decisión

Mantener `revision-service` (puerto 8083, `revision.db`) para recibir cambios de estado, crear/cancelar revisiones pendientes y permitir que el revisor tome la pregunta o emita un dictamen. Llama al Banco con el token del revisor; Banco conserva la autoridad sobre estados.

## Alternativas consideradas

Mantener un servicio independiente de validación como cuarto servicio (diseño inicial) o reemplazarlo por revisión por pares. No se agregan otras alternativas.

## Consecuencias

El flujo cubre parte de HU05, pero la asignación de revisores y el formato completo de evaluación siguen fuera de alcance. La llamada REST y la persistencia de Revisión no comparten transacción con Banco.
