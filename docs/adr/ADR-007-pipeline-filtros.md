# ADR-007: Pipeline de filtros para validar preguntas

- **Estado:** Aceptada
- **Fecha:** 2026-11-19 (historial Git: `3832389`, tubería integrada en Banco)

## Contexto

La validación estructural consta de reglas separadas y ordenadas. La decisión inicial de patrones y la decisión final de iteración 2 identifican Pipeline/Tuberías y Filtros; véanse `docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf`, sección de validación y `docs/decisiones-iteracion-2.md`, §2.

## Decisión

Encadenar siete filtros en `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/pipeline/QuestionPipeline.java`: contexto, pregunta directa, número de opciones, expresiones prohibidas, formato de distractores, respuesta correcta y campos obligatorios.

## Alternativas consideradas

La fuente inicial contrapone validaciones monolíticas/condicionales a filtros separados; el código y las decisiones de iteración 2 conservan el Pipeline. No se inventan alternativas adicionales.

## Consecuencias

Cada regla tiene clase propia y pruebas unitarias; el orden forma parte del comportamiento. El pipeline se ejecuta dentro del proceso Banco y no es un servicio separado.
