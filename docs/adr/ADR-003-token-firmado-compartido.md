# ADR-003: Token firmado y verificado con secreto compartido

- **Estado:** Aceptada
- **Fecha:** 2026-11-18 (historial Git: `f70ec09`, integración del `auth-service`; formato actual descrito en `docs/decisiones-iteracion-2.md`)

## Contexto

Los servicios requieren identificar usuario y rol sin consultar a Auth en cada solicitud. El formato común y la decisión stateless constan en `docs/decisiones-iteracion-2.md`, §4, y D-003 del SAD (`docs_2/SAD.pdf`, §8). La propiedad utilizada es `auth.token.secret` en los cuatro servicios.

## Decisión

Auth emite tokens con payload codificado en base64url (`login|ROL|vencimiento`) y firma HMAC-SHA256. Banco, Revisión y Auditoría verifican firma y vencimiento localmente con el secreto compartido. Fuentes de código: `auth-service/.../service/TokenSigner.java` y `*/security/TokenVerifier.java`.

## Alternativas consideradas

Las fuentes especificadas documentan token compartido frente a validación remota en cada servicio; no se agregan alternativas externas.

## Consecuencias

Se evita una llamada síncrona a Auth por petición, a costa de distribuir una configuración secreta y coordinar su rotación. Los verificadores deben conservar el mismo formato y criterio de reloj. El valor actual es de desarrollo y no debe asumirse válido para producción.
