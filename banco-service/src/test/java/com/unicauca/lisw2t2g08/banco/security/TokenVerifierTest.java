package com.unicauca.lisw2t2g08.banco.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.unicauca.lisw2t2g08.banco.domain.AuthToken;
import com.unicauca.lisw2t2g08.banco.domain.Role;

import static org.junit.jupiter.api.Assertions.*;

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
        fixedClock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));
        verifier = new TokenVerifier(SECRET, fixedClock);
    }

    @Test
    void verify_validToken_returnsAuthToken() {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
        String token = createToken("docente1", Role.AUTOR_PREGUNTAS, future, SECRET);

        Optional<AuthToken> result = verifier.verify(token);

        assertTrue(result.isPresent());
        assertEquals("docente1", result.get().getSubject());
        assertEquals(Role.AUTOR_PREGUNTAS, result.get().getRole());
        assertEquals(future, result.get().getExpiresAt());
    }

    @Test
    void verify_bearerPrefix_returnsAuthToken() {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
        String token = "Bearer " + createToken("admin1", Role.ADMINISTRADOR, future, SECRET);

        Optional<AuthToken> result = verifier.verify(token);

        assertTrue(result.isPresent());
        assertEquals("admin1", result.get().getSubject());
        assertEquals(Role.ADMINISTRADOR, result.get().getRole());
    }

    @Test
    void verify_expiredToken_returnsEmpty() {
        LocalDateTime past = LocalDateTime.now(fixedClock).minusMinutes(5);
        String token = createToken("docente1", Role.DOCENTE, past, SECRET);

        Optional<AuthToken> result = verifier.verify(token);

        assertTrue(result.isEmpty());
    }

    @Test
    void verify_tamperedSignature_returnsEmpty() {
        LocalDateTime future = LocalDateTime.now(fixedClock).plusMinutes(30);
        String token = createToken("docente1", Role.DOCENTE, future, "otro-secreto-diferente-123456");

        Optional<AuthToken> result = verifier.verify(token);

        assertTrue(result.isEmpty());
    }

    @Test
    void verify_malformedToken_returnsEmpty() {
        assertTrue(verifier.verify(null).isEmpty());
        assertTrue(verifier.verify("").isEmpty());
        assertTrue(verifier.verify("sin_punto").isEmpty());
        assertTrue(verifier.verify("a.b.c").isEmpty());
    }

    @Test
    void verify_tokenDelVectorDeContratoCompartido_returnsAuthToken() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneId.of("UTC"));
        TokenVerifier contractVerifier = new TokenVerifier(SECRET, clock);

        AuthToken token = contractVerifier.verify(FIXED_TOKEN).orElseThrow();

        assertEquals("ana", token.getSubject());
        assertEquals(Role.ADMINISTRADOR, token.getRole());
        assertEquals(LocalDateTime.parse("2026-11-04T13:00"), token.getExpiresAt());
    }

    private String createToken(String subject, Role role, LocalDateTime expiresAt, String secretKey) {
        String payload = subject + "|" + role.name() + "|" + expiresAt;
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
