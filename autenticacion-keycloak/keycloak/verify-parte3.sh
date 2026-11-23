#!/usr/bin/env bash
# Runs the table of Parte 3 against the real system (Keycloak + gateway + microservices running).
# Prints one line per case with the expected and the obtained HTTP status.
set -uo pipefail

TOKEN_URL="${TOKEN_URL:-http://localhost:8080/realms/sistema/protocol/openid-connect/token}"
GATEWAY="${GATEWAY:-http://localhost:8081}"
FAILED=0

# token_for <user> -> prints the access token (password is 123 for every user)
token_for() {
    curl -s -X POST "$TOKEN_URL" \
        -d grant_type=password -d client_id=sistema-desktop -d "username=$1" -d password=123 \
        | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

# check <label> <user|none> <path> <expected status>
check() {
    local label="$1" user="$2" path="$3" expected="$4" status
    if [ "$user" = "none" ]; then
        status=$(curl -s -o /dev/null -w '%{http_code}' "$GATEWAY$path")
    else
        status=$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $(token_for "$user")" "$GATEWAY$path")
    fi
    local verdict=OK
    if [ "$status" != "$expected" ]; then verdict=FAIL; FAILED=1; fi
    printf '%-4s %-9s %-30s esperado=%s obtenido=%s  (%s)\n' "$verdict" "$user" "$path" "$expected" "$status" "$label"
}

check "student -> endpoint student"      juan     /a/hello_student     200
check "student -> endpoint coordinator"  juan     /a/hello_coordinator 403
check "coordinator -> endpoint coord."   julio    /b/hello_coordinator 200
check "company -> endpoint company"      company1 /b/hello_company     200
check "cualquiera -> hello_all (b)"      juan     /b/hello_all         200
check "cualquiera -> hello_all (a)"      julio    /a/hello_all         200
check "sin token -> student"             none     /a/hello_student     401
check "sin token -> hello_all"           none     /b/hello_all         401

exit $FAILED
