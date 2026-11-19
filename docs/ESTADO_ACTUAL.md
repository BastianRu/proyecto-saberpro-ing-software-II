# Estado actual del repositorio (línea base)

> Fecha de verificación: 2026-11-18
> Rama de trabajo: `integracion-iteracion2` (creada desde `main`).
> No se hizo `push`, no se modificó `main` y no se tocaron las ramas de talleres.

## 1. Ramas encontradas

| Rama | Uso |
|---|---|
| `main` | Iteración 1 (monolito JavaFX en `src/`) + `banco-service` + `validacion-service`. |
| `origin/segunda_iteracion` | Contiene `auth-service` aislado; no contiene los servicios actuales de `main`. |
| `origin/ddd-hexagonal-simulacros` | Taller de laboratorio. **No forma parte de este trabajo.** |
| `origin/taller10-jpa` | Taller de laboratorio. **No forma parte de este trabajo.** |

## 2. Servicios y puertos verificados en `main`

| Servicio | Puerto | ¿Está en `main`? | Observación |
|---|---:|---|---|
| `auth-service` | 8081 | No | Solo existe en `origin/segunda_iteracion`. |
| `validacion-service` | 8083 | Sí | `POST /validaciones`, sin base de datos. |
| `banco-service` | 8082 | Sí | SQLite `banco.db`, consulta a validación y publicación de eventos. |

## 3. Pruebas ejecutadas antes de cualquier cambio

Comando usado en cada módulo:

```bash
mvn -q test
```

| Módulo | Pruebas ejecutadas | Fallos | Errores | Resultado |
|---|---:|---:|---:|---|
| `validacion-service` | 44 | 0 | 0 | Pasa |
| `banco-service` | 24 | 0 | 0 | Pasa |

Desglose de `banco-service`: `SQLiteQuestionRepositoryTest` (3), `QuestionControllerTest` (8),
`TokenVerifierTest` (5) y `QuestionServiceTest` (8).

`auth-service` no se pudo ejecutar en `main` porque el módulo no existe en esa rama. Se
integró en la Fase 1 y allí se verificó su suite (20 pruebas).

## 4. Diferencias frente al contexto entregado

Se encontraron las siguientes diferencias. No se corrigieron por iniciativa propia; se
registran aquí y, cuando aplica, en `docs/PENDIENTES.md`.

1. **`RULES.md` no existe** en ninguna rama del repositorio, aunque el contexto lo cita como
   fuente de cuerpos de ejemplo y códigos de error. Se creó en la Fase 6 a partir del código
   y de los README verificados.
2. **`validacion-service` usa Spring Boot 3.4.5**, no 3.3.5 como el resto. No se cambió la
   versión porque no hacía falta para las tareas y hacerlo implicaba un riesgo innecesario.
3. **No se entregó el paquete `docs_latex_iteracion2_v2.zip`** en el entorno de trabajo, por
   lo que no hay carpeta `docs/latex/`. Queda registrado en pendientes.
4. **El JDK del entorno es 27**, aunque los `pom.xml` declaran `java.version=17`. Las pruebas
   pasan igualmente; no se modificaron los `pom.xml` por este motivo.
5. **No hay consumidores de los eventos RabbitMQ** (`questions.exchange`). El banco publica
   y nadie escucha. Se documenta en pendientes, sin crear servicios por iniciativa propia.
6. **`RULES.md` y los scripts de punta a punta no existían**; se crean en esta rama.
