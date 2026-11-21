package com.unicauca.lisw2t2g08.banco.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenVerifierTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    // Vector de contrato compartido con auth-service (TokenSignerTest).
    // Se genero con HMAC-SHA256 sobre "ana|ADMINISTRADOR|2026-11-04T13:00"
    // y el mismo secreto. Cambiar el formato en cualquiera de los dos lados
    // debe romper esta prueba.
    private static final String FIXED_TOKEN =
            "YW5hfEFETUlOSVNUUkFET1J8MjAyNi0xMS0wNFQxMzowMA"
            + ".qv9ZN9sRkJ-MC4DwFLI-x-DKWD2IO0InnEnl8Cu-yiQ";

    private Clock fixedClock;
    private TokenVerifier verifier;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
        verifier = new TokenVerifier(SECRET, fixedClock);
    }

    @Test
    void tokenValidoDevuelveElUsuarioYElRol() {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
        String token = createToken("docente1", "AUTOR_PREGUNTAS", future, SECRET);

        Optional<AuthenticatedUser> result = verifier.verify(token);

        assertTrue(result.isPresent());
        assertEquals("docente1", result.get().login());
        assertEquals("AUTOR_PREGUNTAS", result.get().role());
        assertEquals(token, result.get().token());
    }

    @Test
    void tokenVencidoNoSeAcepta() {
        LocalDateTime past = LocalDateTime.now(fixedClock).minusMinutes(5);
        String token = createToken("docente1", "DOCENTE", past, SECRET);

        assertTrue(verifier.verify(token).isEmpty());
    }

    @Test
    void firmaAlteradaNoSeAcepta() {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
        String token = createToken("docente1", "DOCENTE", future, "otro-secreto-diferente-123456");

        assertTrue(verifier.verify(token).isEmpty());
    }

    @Test
    void tokenMalFormadoNoSeAceptaNiLanzaExcepcion() {
        assertTrue(verifier.verify(null).isEmpty());
        assertTrue(verifier.verify("").isEmpty());
        assertTrue(verifier.verify("sin_punto").isEmpty());
        assertTrue(verifier.verify("a.b.c").isEmpty());
    }

    @Test
    void tokenDelVectorDeContratoCompartidoSeAcepta() {
        AuthenticatedUser user = verifier.verify(FIXED_TOKEN).orElseThrow();

        assertEquals("ana", user.login());
        assertEquals("ADMINISTRADOR", user.role());
    }

    @Test
    void secretoCortoSeRechaza() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TokenVerifier("corto", fixedClock));
    }

    private String createToken(String subject, String role, LocalDateTime expiresAt, String secretKey) {
        String payload = subject + "|" + role + "|" + expiresAt;
        String encodedPayload = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
