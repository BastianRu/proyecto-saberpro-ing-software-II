# Pendientes y decisiones que faltan

> Este archivo registra lo que **no** se implementó por depender de una decisión
> que no está tomada. No se creó ningún servicio ni se cambió ningún contrato
> por iniciativa propia.

## 1. Servicio REVISOR sugerido por el profesor

**Estado:** no definido.

El profesor sugirió un servicio REVISOR a cambio de otro; falta confirmar cuál
servicio reemplaza y qué funciones tiene.

Preguntas concretas:

1. ¿Qué servicio de los actuales (`auth-service`, `validacion-service`,
   `banco-service`) reemplazaría el servicio REVISOR?
2. ¿Qué funciones de negocio debe cubrir? Por ejemplo: asignar preguntas a
   revisores, registrar observaciones, aprobar/rechazar, notificar al autor.
3. ¿Necesita base de datos propia o puede apoyarse en `banco-service`?
4. ¿El rol `REVISOR` que ya existe en `auth-service` y en `banco-service` se
   mantiene tal cual?

Mientras no haya respuesta, no se creó `revisor-service`. El rol `REVISOR` ya
existe y `banco-service` ya le permite cambiar el estado de una pregunta
(`PATCH /preguntas/{id}/estado`), pero no hay un servicio dedicado a la revisión.

## 2. Eventos publicados sin consumidor

**Estado:** sin decisión.

`banco-service` publica `QUESTION_CREATED`, `QUESTION_UPDATED` y
`QUESTION_STATE_CHANGED` en el intercambio `questions.exchange`, con claves
`question.question_created`, `question.question_updated` y
`question.question_state_changed`. **Hoy no existe ningún consumidor** ni cola
declarada por los servicios del repositorio.

Preguntas concretas:

1. ¿Se crea un servicio de auditoría que consuma y persista los eventos?
2. ¿Se crea un servicio de notificaciones (correo o interfaz) para avisar a los
   autores y revisores?
3. ¿O los eventos quedan solo como registro para una integración futura?

No se creó un consumidor por iniciativa propia.

## 3. Paquete LaTeX de la iteración 2

**Estado:** no entregado en el entorno de trabajo.

El prompt menciona `docs_latex_iteracion2_v2.zip` (documentos LaTeX y
`RULES.md`), pero el archivo no estaba disponible en la copia local. Por eso:

- no existe `docs/latex/`;
- `RULES.md` se reconstruyó a partir del código y de los README, no del paquete.

**Pregunta:** ¿se debe entregar el ZIP para colocarlo en `docs/latex/` sin
modificar su contenido y contrastar `RULES.md` con la versión oficial?

## 4. Secreto del token en los archivos de configuración

**Estado:** valor de desarrollo.

`auth-service` y `banco-service` traen el mismo secreto por defecto
(`dev-secret-cambiar-en-produccion-0123456789`) en sus
`application.properties`. Es cómodo para ejecutar en local, pero no debe llegar
a producción.

**Pregunta:** ¿se acepta mantener el valor de desarrollo para la entrega
académica o se exige inyectarlo por variable de entorno y dejar el archivo sin
secreto?

No se cambió el mecanismo de configuración porque no hay una decisión sobre
despliegue.

## 5. Versión de Spring Boot en `validacion-service`

**Estado:** diferencia detectada.

`auth-service` y `banco-service` usan Spring Boot **3.3.5**;
`validacion-service` usa **3.4.5**. Las pruebas pasan en ambos casos.

**Pregunta:** ¿se debe alinear `validacion-service` a 3.3.5 para que los tres
módulos usen la misma versión? No se cambió porque no era necesario para las
tareas y podía introducir diferencias en las pruebas.

## 6. Zona horaria del token

**Estado:** observación, sin cambio.

El token guarda `expiresAt` como `LocalDateTime` sin zona horaria y ambos
servicios lo comparan con su reloj local. Funciona si `auth-service` y
`banco-service` comparten zona horaria (el caso actual, un solo equipo). Si se
despliegan en zonas distintas, un token podría vencer antes o después de lo
esperado.

**Pregunta:** ¿se mantiene el formato actual por simplicidad académica o se
migra a un instante con zona (`Instant`/`ZonedDateTime`) en una próxima
iteración? Cambiarlo ahora rompería el contrato del token y la prueba de vector
fijo, por lo que no se hizo.

## 7. Script de punta a punta en Windows

**Estado:** solo existe `scripts/e2e.sh`.

Se entregó el script para Bash (Linux/macOS/WSL). No se creó una versión
PowerShell porque el repositorio no tiene otros scripts `.ps1` y no se pidió
explícitamente una para Windows.

**Pregunta:** ¿se necesita `scripts/e2e.ps1` para el equipo que trabaja en
Windows?
