# ADR-004: Validación estructural embebida en Banco

- **Estado:** Aceptada; reemplaza la decisión inicial de validación independiente
- **Fecha:** 2026-11-19 (historial Git: `3832389`, Banco con validación embebida)

## Contexto

El diseño inicial proponía validación estructural como servicio independiente. El SAD recoge la validación embebida en D-002 (`entregables/sprint-2/SAD_Banco_Preguntas_Iteracion2.pdf`, §8); `docs/decisiones-iteracion-2.md`, §§2–3 explica que el pipeline es una función sobre el borrador y la llamada de red agregaba dependencia y punto de fallo.

## Decisión

Ejecutar localmente en `banco-service` la secuencia de siete filtros de `banco-service/src/main/java/com/unicauca/lisw2t2g08/banco/pipeline/QuestionPipeline.java`. No hay un servicio de validación de producción.

## Alternativas consideradas

Servicio independiente invocado por REST, documentado como diseño inicial (`docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf`; `docs/decisiones-iteracion-2.md`, §2), frente al pipeline embebido final.

## Consecuencias

Crear/editar preguntas evita una llamada de red al validador; las reglas y su operación quedan en Banco. Los filtros no pueden escalar o desplegarse por separado. El antiguo directorio `validacion-service/` ya no forma parte del repositorio.
