package com.unicauca.lisw2t2g08.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;
import com.unicauca.lisw2t2g08.auth.domain.UserStatus;

class TokenSignerTest {

    private static final Instant NOW = Instant.parse("2026-10-31T10:00:00Z");

    // Vector de contrato compartido con banco-service (TokenVerifierTest).
    // Cualquier cambio en el formato del payload o en el algoritmo de firma
    // debe romper esta prueba y la del otro servicio.
    private static final String FIXED_SECRET = "0123456789abcdef0123456789abcdef";
    private static final String FIXED_TOKEN =
            "YW5hfEFETUlOSVNUUkFET1J8MjAyNi0xMS0wNFQxMzowMA"
            + ".qv9ZN9sRkJ-MC4DwFLI-x-DKWD2IO0InnEnl8Cu-yiQ";

    private final User user = new User("jperez", "Juan", Role.REVISOR, UserStatus.ACTIVO, "hash");

    private TokenSigner signerAt(Instant instant, String secret) {
        return new TokenSigner(secret, Duration.ofMinutes(30), Clock.fixed(instant, ZoneOffset.UTC));
    }

    private String issueCompact(TokenSigner signer) {
        return signer.compact(signer.issue(user));
    }

    @Test
    void tokenEmitidoSeVerificaYConservaSusDatos() {
        TokenSigner signer = signerAt(NOW, "secreto-de-prueba-123456");

        AuthToken verified = signer.verify(signer.compact(signer.issue(user))).orElseThrow();

        assertEquals("jperez", verified.getSubject());
        assertEquals(Role.REVISOR, verified.getRole());
    }

    @Test
    void tokenConElPayloadAlteradoNoSeVerifica() {
        TokenSigner signer = signerAt(NOW, "secreto-de-prueba-123456");
        String[] parts = signer.compact(signer.issue(user)).split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("jperez|ADMINISTRADOR|2099-01-01T00:00".getBytes());

        assertFalse(signer.verify(forgedPayload + "." + parts[1]).isPresent());
    }

    @Test
    void tokenFirmadoConOtroSecretoNoSeVerifica() {
        String compact = issueCompact(signerAt(NOW, "secreto-de-prueba-123456"));
        assertFalse(signerAt(NOW, "otro-secreto-distinto-999").verify(compact).isPresent());
    }

    @Test
    void tokenVencidoNoSeVerifica() {
        String compact = issueCompact(signerAt(NOW, "secreto-de-prueba-123456"));
        TokenSigner later = signerAt(NOW.plus(Duration.ofMinutes(31)), "secreto-de-prueba-123456");
        assertFalse(later.verify(compact).isPresent());
    }

    @Test
    void basuraNoSeVerificaNiLanzaExcepcion() {
        TokenSigner signer = signerAt(NOW, "secreto-de-prueba-123456");
        assertFalse(signer.verify("no-es-un-token").isPresent());
        assertFalse(signer.verify("a.b").isPresent());
        assertFalse(signer.verify("").isPresent());
    }

    @Test
    void secretoCortoSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> signerAt(NOW, "corto"));
    }

    @Test
    void tokenDeContratoCoincideConElVectorCompartido() {
        Clock fixed = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
        TokenSigner signer = new TokenSigner(FIXED_SECRET, Duration.ofMinutes(60), fixed);
        User ana = new User("ana", "Ana", Role.ADMINISTRADOR, UserStatus.ACTIVO, "hash");

        String compact = signer.compact(signer.issue(ana));

        assertEquals(FIXED_TOKEN, compact);
        assertEquals("ana", signer.verify(compact).orElseThrow().getSubject());
    }
}
