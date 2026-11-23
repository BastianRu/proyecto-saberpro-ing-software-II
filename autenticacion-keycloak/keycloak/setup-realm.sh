#!/usr/bin/env bash
# Configures Keycloak for Taller 12 (Parte 1) with the admin CLI (kcadm.sh):
#   realm "sistema", client "sistema-desktop" (Direct access grants), client roles
#   student / coordinator / company, and the users juan, julio and company1.
#
# Usage (Keycloak already running on http://localhost:8080):
#   KEYCLOAK_HOME=/path/to/keycloak-25.0.6 ./setup-realm.sh
# Optional: KEYCLOAK_URL, KEYCLOAK_ADMIN, KEYCLOAK_ADMIN_PASSWORD.
set -euo pipefail

KEYCLOAK_HOME="${KEYCLOAK_HOME:?Set KEYCLOAK_HOME to the Keycloak installation directory}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
KCADM="$KEYCLOAK_HOME/bin/kcadm.sh"
REALM="sistema"
CLIENT="sistema-desktop"

echo "== Authenticating the admin CLI against $KEYCLOAK_URL"
"$KCADM" config credentials --server "$KEYCLOAK_URL" --realm master \
    --user "$ADMIN_USER" --password "$ADMIN_PASSWORD"

echo "== Paso 4: realm '$REALM'"
"$KCADM" create realms -s realm="$REALM" -s enabled=true

echo "== Paso 5: client '$CLIENT' with Direct access grants"
"$KCADM" create clients -r "$REALM" \
    -s clientId="$CLIENT" -s protocol=openid-connect -s enabled=true \
    -s publicClient=true -s directAccessGrantsEnabled=true -s standardFlowEnabled=true
CLIENT_UUID="$("$KCADM" get clients -r "$REALM" -q clientId="$CLIENT" --fields id --format csv --noquotes)"

echo "== Paso 6: client roles"
"$KCADM" create "clients/$CLIENT_UUID/roles" -r "$REALM" -s name=student -s description="Rol estudiante"
"$KCADM" create "clients/$CLIENT_UUID/roles" -r "$REALM" -s name=coordinator -s description="Coordinador del PIS"
"$KCADM" create "clients/$CLIENT_UUID/roles" -r "$REALM" -s name=company -s description="Empresa"

# create_user <username> <first name> <last name> <role>
create_user() {
    local username="$1" first="$2" last="$3" role="$4"
    "$KCADM" create users -r "$REALM" -s username="$username" -s enabled=true -s emailVerified=true \
        -s email="$username@example.com" -s firstName="$first" -s lastName="$last"
    "$KCADM" set-password -r "$REALM" --username "$username" --new-password 123
    "$KCADM" add-roles -r "$REALM" --uusername "$username" --cclientid "$CLIENT" --rolename "$role"
}

echo "== Paso 7: users"
create_user juan Juan Estudiante student
create_user julio Julio Coordinador coordinator
create_user company1 Empresa Uno company

echo "== Done. Token endpoint: $KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token"
