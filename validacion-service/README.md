# validacion-service

Microservicio de **Validacion Estructural** (HU03) del proyecto *Banco de
Preguntas Saber Pro* (Ingenieria de Software II, Universidad del Cauca).

Expone como API REST el pipeline de filtros que en la iteracion 1 vivia dentro
del monolito JavaFX. El servicio **no tiene base de datos, no es dueno de datos
y no llama a otros servicios**: recibe un borrador de pregunta, lo hace
atravesar los siete filtros y responde si es valido y, si no lo es, con los
mensajes de error producidos por el primer filtro que falla.

## Que valida

El pipeline se ejecuta en el mismo orden y con la misma logica y mensajes que el
monolito:

| Orden | Filtro | Requisito | Regla |
|---|---|---|---|
| 1 | `ContextFilter` | RF-08 | Contexto presente, de al menos 20 caracteres. |
| 2 | `DirectQuestionFilter` | RF-09 | Una unica pregunta directa, que termina en `?` y no tiene mas de un `?`. |
| 3 | `DistractorCountFilter` | RF-10 | Exactamente cuatro distractores (cinco opciones con la correcta). |
| 4 | `ForbiddenExpressionFilter` | RF-12 | Prohibidas "Todas las anteriores" y "Ninguna de las anteriores". |
| 5 | `DistractorFormatFilter` | RF-13 | Opciones no vacias, de al menos 3 caracteres, con mayuscula inicial y sin repetirse. |
| 6 | `CorrectAnswerFilter` | RF-11 | Una unica respuesta correcta, con indice valido y no repetida. |
| 7 | `RequiredFieldsFilter` | RF-05 | Nombre, justificacion, bibliografia, competencia, tema, subtema y dificultad. |

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- Conexion a internet la primera vez, para descargar las dependencias de Spring
  Boot.

## Compilar y probar

Desde la carpeta `validacion-service`:

```bash
mvn test
```

Ejecuta las 36 pruebas migradas del pipeline y las 8 pruebas de contrato de la
API (44 en total).

## Ejecutar

```bash
mvn spring-boot:run
```

El servicio queda escuchando en `http://localhost:8083`. No requiere base de
datos ni variables de entorno.

## Contrato

### `POST /validaciones`

Recibe un borrador y responde **siempre** con el cuerpo:

```json
{
  "valid": true,
  "errors": []
}
```

| Codigo | Cuando | Cuerpo |
|---|---|---|
| `200` | El borrador es valido. | `{"valid": true, "errors": []}` |
| `422` | Algun filtro falla (`ValidationException`). | `{"valid": false, "errors": ["<mensajes del filtro>"]}` |
| `400` | JSON mal formado, cuerpo vacio o `difficulty` desconocido. | `{"valid": false, "errors": ["Solicitud mal formada"]}` |
| `500` | Error inesperado. | `{"valid": false, "errors": ["Error interno"]}` |

Los mensajes de `errors` son los que produce cada filtro (por ejemplo
`RF-11: debe indicarse cual opcion es la respuesta correcta`); no se reescriben
en la capa API.

Los valores admitidos en `difficulty` son `BAJA`, `MEDIA` y `ALTA`.

### Ejemplo valido (`200`)

```bash
curl -i -X POST http://localhost:8083/validaciones \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Ventaja del patron Pipeline",
    "context": "El patron Pipeline encadena filtros que procesan datos en secuencia.",
    "directQuestion": "Cual es la ventaja principal de este patron?",
    "options": [
      "Permite componer validaciones independientes",
      "Obliga a reescribir todo el sistema",
      "Elimina la necesidad de pruebas",
      "Impide reutilizar componentes",
      "Requiere una base de datos distribuida"
    ],
    "correctIndex": 0,
    "justification": "El patron permite componer validaciones independientes.",
    "bibliography": "Gamma et al. Design Patterns.",
    "competence": "Diseno de software",
    "topic": "Patrones de diseno",
    "subtopic": "Pipeline",
    "difficulty": "MEDIA"
  }'
```

Respuesta:

```json
{"valid":true,"errors":[]}
```

### Ejemplo invalido (`422`)

La ultima opcion usa una expresion prohibida (RF-12):

```bash
curl -i -X POST http://localhost:8083/validaciones \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Ventaja del patron Pipeline",
    "context": "El patron Pipeline encadena filtros que procesan datos en secuencia.",
    "directQuestion": "Cual es la ventaja principal de este patron?",
    "options": [
      "Permite componer validaciones independientes",
      "Obliga a reescribir todo el sistema",
      "Elimina la necesidad de pruebas",
      "Impide reutilizar componentes",
      "Todas las anteriores"
    ],
    "correctIndex": 0,
    "justification": "El patron permite componer validaciones independientes.",
    "bibliography": "Gamma et al. Design Patterns.",
    "competence": "Diseno de software",
    "topic": "Patrones de diseno",
    "subtopic": "Pipeline",
    "difficulty": "MEDIA"
  }'
```

Respuesta:

```json
{"valid":false,"errors":["RF-12: no se permite la expresion \"Todas las anteriores\""]}
```

### Ejemplo con dificultad desconocida (`400`)

```bash
curl -i -X POST http://localhost:8083/validaciones \
  -H 'Content-Type: application/json' \
  -d '{"difficulty": "EXPERTO"}'
```

Respuesta:

```json
{"valid":false,"errors":["Solicitud mal formada"]}
```

## Coleccion Postman

La coleccion `postman/validacion-service.postman_collection.json` contiene las
mismas peticiones de ejemplo. Importala en Postman y ejecutala contra
`http://localhost:8083`.

## Estructura

```
validacion-service/
├── pom.xml
├── README.md
├── postman/
│   └── validacion-service.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/unicauca/lisw2t2g08/validacion/
    │   │   ├── ValidacionServiceApplication.java
    │   │   ├── api/       (controller, DTOs, manejador de errores, configuracion)
    │   │   ├── domain/    (QuestionDraft, DifficultyLevel)
    │   │   └── pipeline/  (filtros, pipeline y ValidationException)
    │   └── resources/
    │       └── application.properties   (server.port=8083)
    └── test/java/com/unicauca/lisw2t2g08/validacion/
        ├── api/       (ValidationControllerTest)
        └── pipeline/  (pruebas migradas del monolito y DraftFactory)
```
