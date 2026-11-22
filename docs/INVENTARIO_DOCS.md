# Inventario de Documentación del Repositorio

> **Fecha de verificación:** 2026-11-22  
> **Rama de trabajo:** `docs-iteracion2`  
> **Equipo:** Juan Manuel Meneses Daza, Juan Sebastian Muñoz Ruiz, Juan Sebastian Molano Bolaños, Santiago Rodríguez Bolaños  
> **Metodología de inspección:** Contraste estricto contra el código fuente (`auth-service`, `banco-service`, `revision-service`, `auditoria-service`, `src/`), `pom.xml`, archivos de configuración y ejecución de pruebas unitarias con Maven.

---

## 1. Tabla de inventario y vigencia

| Documento | Qué cubre | Vigencia | Evidencia y justificación |
|---|---|---|---|
| `README.md` (raíz) | Resumen del proyecto, descripción del monolito en `src/` (corte 1) y resumen general de los cuatro microservicios de la iteración 2. | **Vigente** | Describe con precisión los cuatro servicios (`auth-service`: 8081, `banco-service`: 8082, `revision-service`: 8083, `auditoria-service`: 8084) y aclara que `src/` es el cliente de la iteración 1. |
| `CONTEXTO_PROYECTO.md` (raíz) | Contexto institucional, evolución arquitectónica, historias de usuario (HU01 a HU04), patrones de diseño, esquema relacional y estructura de directorios. | **Vigente** | Refleja la arquitectura de 4 servicios, el pipeline embebido en el Banco, el patrón State polimórfico, RabbitMQ y la colección unificada de Postman. |
| `RULES.md` (raíz) | Reglas de negocio y contrato técnico de validación, usuarios, token, banco, seguridad y mensajería. | **Vigente tras actualización** | Se reemplazó la descripción obsoleta de `validacion-service` por las reglas de validación embebida, cuatro servicios, roles y eventos actuales; ver `RULES.md` y `docs/HALLAZGOS.md` para el registro de contradicciones históricas. |
| `docker-compose.yml` | Despliegue del broker RabbitMQ con plugin de gestión (`rabbitmq:3.13-management`). | **Vigente** | Expone puertos 5672 (AMQP) y 15672 (HTTP UI). No incluye contenedores para los microservicios (solo el broker). |
| `scripts/e2e.sh` | Script Bash para pruebas de integración de punta a punta. | **Desactualizado** | Define variables para `VALIDACION_URL=http://localhost:8083`, llama a endpoints de validación externa y a rutas de historial de banco hoy inexistentes. |
| `postman/saberpro-microservicios.postman_collection.json` | Colección completa de peticiones HTTP en Postman para probar el flujo entre los cuatro servicios. | **Vigente** | Probada y consistente con los endpoints reales de los 4 servicios (puertos 8081, 8082, 8083 y 8084). |
| `auth-service/README.md` | Documentación técnica del servicio de autenticación y usuarios: endpoints, formato del token HMAC-SHA256, Argon2id y pruebas. | **Vigente** | Coincide exactamente con el código de `auth-service`. 21 pruebas unitarias pasando. |
| `banco-service/README.md` | Documentación de preguntas, pipeline embebido de 7 filtros, jerarquía de estados State, eventos y matriz de roles. | **Vigente** | Coincide con la implementación de `banco-service`. 94 pruebas unitarias pasando. |
| `revision-service/README.md` | Documentación de revisión por pares: consumidor de `question.state-changed`, endpoints REST y delegación al Banco. | **Vigente** | Coincide con la implementación de `revision-service`. 40 pruebas unitarias pasando. |
| `auditoria-service/README.md` | Documentación del servicio de historial: consumidor de `question.*`, deduplicación por `event_id` y consulta cronológica. | **Vigente** | Coincide con la implementación de `auditoria-service`. 9 pruebas unitarias pasando. |
| `docs/decisiones-iteracion-2.md` | Registro de decisiones arquitectónicas de la iteración 2: alcance HU01-HU04 (+HU05 parcial), diferencias, contratos de token y eventos, limitaciones de escalabilidad y SQLite. | **Vigente** | Fuente fidedigna de las decisiones finales tomadas por el equipo. |
| `docs/PENDIENTES.md` | Lista de dudas y decisiones no implementadas. | **Vigente tras actualización** | Se eliminaron dudas ya resueltas (servicio REVISOR, consumidores) y se registraron brechas actuales: cliente, outbox, DLQ, SQLite, persistencia del broker, CI, Jira y alcance parcial HU05. |
| `docs/ESTADO_ACTUAL.md` | Diagnóstico de línea base fechado al 2026-11-18 en la rama preliminar `integracion-iteracion2`. | **Histórico** | Se encabezó como histórico explícito: refleja un estado intermedio en el que `auth-service` no estaba en `main` y existía `validacion-service`. |
| `docs/EJECUCION.md` | Guía de arranque manual de los servicios y variables de configuración. | **Vigente tras actualización** | Guía reemplazada por los cuatro servicios y sus puertos; advierte que el script E2E sigue desactualizado y que Compose solo levanta RabbitMQ. |
| `docs/REDUNDANCIAS.md` | Análisis de clases duplicadas entre servicios (contratos sin librería compartida). | **Vigente tras actualización** | Sustituido el análisis del validador obsoleto por las copias locales de roles, token, eventos y referencias lógicas entre los cuatro servicios. |
| `docs/COMPARACION_INTEGRACION.md` | Análisis comparativo entre la rama local previa y la versión actualizada de `origin/main` (`97d3233`). | **Histórico** | Se encabezó como histórico y se conserva como trazabilidad de integración del 2026-11-20, no como guía de ejecución vigente. |
| `docs/MiniQAW_Banco_Preguntas_Saber_Pro.xlsx` | Hoja de cálculo original del taller QAW con escenarios, diagrama de empatía y priorización. | **Histórico (Base corte 1)** | Se conserva intacta como insumo del corte 1; contiene escenarios de monolito y medidas técnicas que no son resultados de Sprint 2. |
| `docs/MiniQAW_Banco_Preguntas_Saber_Pro_v2.xlsx` | Copia de trabajo con contexto de iteración 2 y propuestas de esfuerzo humano en escenarios refinados. | **Vigente como borrador** | Generada desde el original con `openpyxl`; cifras y duraciones etiquetadas como propuestas por validar, no medidas. |
| `docs/Documento_Arquitectura_Banco_Preguntas_Saber_Pro.pdf` | Documento de arquitectura entregado en la iteración 1 (primer corte, monolito JavaFX en capas). | **Histórico** | Describe la iteración 1 del monolito local en JavaFX. Se mantiene por trazabilidad académica. |
| `docs/Decisiones_Tecnicas_Banco_Preguntas_Saber_Pro.pdf` | Documento de decisiones técnicas iniciales del primer corte. | **Histórico** | Sirve como fuente de contraste directo para el documento `Decisiones_Iniciales_vs_Finales.tex`. |
| `docs_2/SAD.docx` y `docs_2/SAD.pdf` | Software Architecture Document de 29 páginas elaborado sobre la plantilla oficial del curso para la iteración 2. | **Vigente (Fuente principal)** | Contiene los diagramas C4 (niveles 1 a 3), diagramas UML, vistas de arquitectura, riesgos y glosario del diseño actual de microservicios. |
| `docs_2/Documento_Arquitectura_Iteracion2_v2.pdf` | Versión preliminar en LaTeX del documento de arquitectura de la segunda iteración. | **Histórico** | Antecedente directo del documento LaTeX final que se genera en `docs/latex/`. |
