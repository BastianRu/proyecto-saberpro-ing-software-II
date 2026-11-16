package com.unicauca.lisw2t2g08.banco.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.unicauca.lisw2t2g08.banco.domain.AuthToken;
import com.unicauca.lisw2t2g08.banco.domain.Role;

/**
 * Verifica localmente los tokens firmados emitidos por auth-service con HMAC-SHA256.
 *
 * Formato compacto esperado: base64url(subject|role|expiresAt) + "." + base64url(firma)
 */
public class TokenVerifier {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final byte[] secret;
    private final Clock clock;

    public TokenVerifier(String secret) {
        this(secret, Clock.systemDefaultZone());
    }

    public TokenVerifier(String secret, Clock clock) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("El secreto del token debe tener al menos 16 caracteres");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    /**
     * Verifica la autenticidad y vigencia de una cadena de token Bearer.
     *
     * @param compactToken token en formato "payload.firma"
     * @return AuthToken si es valido, o Optional.empty() si es invalido o expiro.
     */
    public Optional<AuthToken> verify(String compactToken) {
        if (compactToken == null || compactToken.isBlank()) {
            return Optional.empty();
        }

        // Si viene con el prefijo "Bearer ", se extrae el token
        String tokenStr = compactToken.startsWith("Bearer ") ? compactToken.substring(7).trim() : compactToken.trim();

        try {
            String[] parts = tokenStr.split("\\.");
            if (parts.length != 2) {
                return Optional.empty();
            }

            String payload = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
            boolean validSignature = MessageDigest.isEqual(
                    sign(payload).getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8));

            if (!validSignature) {
                return Optional.empty();
            }

            String[] fields = payload.split("\\|");
            if (fields.length != 3) {
                return Optional.empty();
            }

            AuthToken token = new AuthToken(
                    fields[0],
                    Role.valueOf(fields[1]),
                    LocalDateTime.parse(fields[2]),
                    parts[1]);

            return token.isExpired(LocalDateTime.now(clock)) ? Optional.empty() : Optional.of(token);
        } catch (RuntimeException e) {
            return Optional.empty(); // Malformado, expirado o rol desconocido
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la firma del token", e);
        }
    }
}
