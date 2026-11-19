#!/usr/bin/env bash
#
# Prueba de punta a punta del Banco de Preguntas Saber Pro.
#
# Requiere los tres microservicios en marcha:
#   auth-service       http://localhost:8081
#   validacion-service http://localhost:8083
#   banco-service      http://localhost:8082
#
# RabbitMQ es opcional: si no esta disponible, banco-service registra una
# advertencia y la operacion continua. Ver docs/EJECUCION.md.
#
# Uso:
#   ./scripts/e2e.sh
#   AUTH_URL=http://otro-host:8081 BANCO_URL=http://otro-host:8082 ./scripts/e2e.sh

set -uo pipefail

AUTH_URL="${AUTH_URL:-http://localhost:8081}"
BANCO_URL="${BANCO_URL:-http://localhost:8082}"
VALIDACION_URL="${VALIDACION_URL:-http://localhost:8083}"

PASS=0
FAIL=0
BODY_FILE="$(mktemp)"
WORK_DIR="$(mktemp -d)"
trap 'rm -f "$BODY_FILE"; rm -rf "$WORK_DIR"' EXIT

SUFFIX="$(date +%s)_$$"
ADMIN_LOGIN="admin_e2e_${SUFFIX}"
DOCENTE_LOGIN="docente_e2e_${SUFFIX}"
PASSWORD="Clave123!"

ok() {
    PASS=$((PASS + 1))
    printf '  [OK]   %s\n' "$1"
}

ko() {
    FAIL=$((FAIL + 1))
    printf '  [FALLA] %s\n' "$1"
    if [ -n "${2:-}" ]; then
        printf '         esperado=%s obtenido=%s\n' "$2" "$3"
    fi
}

# request METHOD URL [TOKEN] [JSON]
# Deja el cuerpo de la respuesta en BODY_FILE y devuelve el codigo HTTP.
request() {
    local method="$1"
    local url="$2"
    local token="${3:-}"
    local data="${4:-}"
    local args=(-sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$url")

    if [ -n "$token" ]; then
        args+=(-H "Authorization: Bearer ${token}")
    fi
    if [ -n "$data" ]; then
        args+=(-H "Content-Type: application/json" --data-binary "$data")
    fi

    local code
    code="$(curl "${args[@]}" 2>/dev/null)" || code="000"
    printf '%s' "$code"
}

# json_field NAME: imprime un campo de primer nivel del ultimo cuerpo.
json_field() {
    python3 - "$BODY_FILE" "$1" <<'PY'
import json
import sys

path, field = sys.argv[1], sys.argv[2]
try:
    with open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    value = data.get(field) if isinstance(data, dict) else None
    if value is not None:
        print(value)
except Exception:
    pass
PY
}

check_status() {
    local description="$1"
    local expected="$2"
    local actual="$3"

    if [ "$expected" = "$actual" ]; then
        ok "$description ($actual)"
    else
        ko "$description" "$expected" "$actual"
        printf '         cuerpo: %s\n' "$(head -c 400 "$BODY_FILE" 2>/dev/null)"
    fi
}

require_tool() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "ERROR: se requiere '$1' para ejecutar la prueba." >&2
        exit 2
    fi
}

require_tool curl
require_tool python3

echo "== Prueba de punta a punta =="
echo "auth:       ${AUTH_URL}"
echo "banco:      ${BANCO_URL}"
echo "validacion: ${VALIDACION_URL}"
echo

# ---------------------------------------------------------------------------
# 0. Disponibilidad basica de auth-service y banco-service
# ---------------------------------------------------------------------------
echo "0) Disponibilidad de los servicios"
check_status "auth-service responde" "200" "$(request GET "${AUTH_URL}/usuarios")"
check_status "banco-service responde" "200" "$(request GET "${BANCO_URL}/preguntas")"

if [ "$FAIL" -gt 0 ]; then
    echo
    echo "Los servicios no estan disponibles. Levantalos segun docs/EJECUCION.md." >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# 1. Registro e inicio de sesion
# ---------------------------------------------------------------------------
echo
echo "1) Registro e inicio de sesion"

REGISTER_ADMIN="$(cat <<JSON
{
  "login": "${ADMIN_LOGIN}",
  "fullName": "Administrador E2E",
  "role": "ADMINISTRADOR",
  "password": "${PASSWORD}"
}
JSON
)"
check_status "registrar administrador" "201" \
    "$(request POST "${AUTH_URL}/usuarios" "" "$REGISTER_ADMIN")"

LOGIN_ADMIN="$(cat <<JSON
{
  "login": "${ADMIN_LOGIN}",
  "password": "${PASSWORD}"
}
JSON
)"
check_status "iniciar sesion como administrador" "200" \
    "$(request POST "${AUTH_URL}/login" "" "$LOGIN_ADMIN")"
TOKEN="$(json_field token)"

REGISTER_DOCENTE="$(cat <<JSON
{
  "login": "${DOCENTE_LOGIN}",
  "fullName": "Docente E2E",
  "role": "DOCENTE",
  "password": "${PASSWORD}"
}
JSON
)"
check_status "registrar docente para probar 403" "201" \
    "$(request POST "${AUTH_URL}/usuarios" "" "$REGISTER_DOCENTE")"

LOGIN_DOCENTE="$(cat <<JSON
{
  "login": "${DOCENTE_LOGIN}",
  "password": "${PASSWORD}"
}
JSON
)"
check_status "iniciar sesion como docente" "200" \
    "$(request POST "${AUTH_URL}/login" "" "$LOGIN_DOCENTE")"
DOCENTE_TOKEN="$(json_field token)"

if [ -z "$TOKEN" ]; then
    echo "ERROR: no se obtuvo el token del administrador; no se puede continuar." >&2
    printf 'cuerpo del login: %s\n' "$(cat "$BODY_FILE" 2>/dev/null)"
    exit 1
fi

# ---------------------------------------------------------------------------
# 2. Creacion y validacion de preguntas
# ---------------------------------------------------------------------------
echo
echo "2) Creacion y validacion de preguntas"

VALID_QUESTION="$(cat <<'JSON'
{
  "name": "Ventaja del patron Pipeline E2E",
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
}
JSON
)"
check_status "crear pregunta valida" "201" \
    "$(request POST "${BANCO_URL}/preguntas" "$TOKEN" "$VALID_QUESTION")"
QUESTION_ID="$(json_field id)"

INVALID_QUESTION="$(cat <<'JSON'
{
  "name": "Pregunta con expresion prohibida E2E",
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
  "justification": "La opcion final usa una expresion prohibida.",
  "bibliography": "Gamma et al. Design Patterns.",
  "competence": "Diseno de software",
  "topic": "Patrones de diseno",
  "subtopic": "Pipeline",
  "difficulty": "MEDIA"
}
JSON
)"
check_status "rechazar pregunta invalida con 422" "422" \
    "$(request POST "${BANCO_URL}/preguntas" "$TOKEN" "$INVALID_QUESTION")"

if [ -z "$QUESTION_ID" ]; then
    echo "ERROR: no se obtuvo el id de la pregunta creada; no se puede continuar." >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# 3. Ciclo de vida y auditoria
# ---------------------------------------------------------------------------
echo
echo "3) Ciclo de vida y auditoria"

CHANGE_OK='{"state":"PENDIENTE_REVISION"}'
check_status "transicion valida BORRADOR -> PENDIENTE_REVISION" "200" \
    "$(request PATCH "${BANCO_URL}/preguntas/${QUESTION_ID}/estado" "$TOKEN" "$CHANGE_OK")"

CHANGE_INVALID='{"state":"PUBLICADA"}'
check_status "transicion invalida PENDIENTE_REVISION -> PUBLICADA" "400" \
    "$(request PATCH "${BANCO_URL}/preguntas/${QUESTION_ID}/estado" "$TOKEN" "$CHANGE_INVALID")"

check_status "consultar historial de la pregunta" "200" \
    "$(request GET "${BANCO_URL}/preguntas/${QUESTION_ID}/historial")"

check_status "consultar estadisticas" "200" \
    "$(request GET "${BANCO_URL}/preguntas/estadisticas")"

# ---------------------------------------------------------------------------
# 4. Seguridad
# ---------------------------------------------------------------------------
echo
echo "4) Seguridad"

check_status "crear pregunta sin token devuelve 401" "401" \
    "$(request POST "${BANCO_URL}/preguntas" "" "$VALID_QUESTION")"

check_status "crear pregunta con rol DOCENTE devuelve 403" "403" \
    "$(request POST "${BANCO_URL}/preguntas" "$DOCENTE_TOKEN" "$VALID_QUESTION")"

# ---------------------------------------------------------------------------
# 5. Resumen
# ---------------------------------------------------------------------------
echo
echo "== Resumen =="
echo "Pasaron: ${PASS}"
echo "Fallaron: ${FAIL}"

if [ "$FAIL" -eq 0 ]; then
    echo "RESULTADO: EXITO"
    exit 0
fi

echo "RESULTADO: FALLO"
exit 1
