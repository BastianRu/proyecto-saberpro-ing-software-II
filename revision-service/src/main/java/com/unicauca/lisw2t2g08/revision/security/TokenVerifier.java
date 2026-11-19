package com.unicauca.lisw2t2g08.revision.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifica localmente los tokens emitidos por el servicio de Usuarios, sin llamarlo en cada petición.
 * Formato: base64url("login|ROL|vence") + "." + base64url(HMAC-SHA256). Requiere el mismo secreto
 * con el que firma el servicio de Usuarios.
 */
public class TokenVerifier {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final Clock clock;

    public TokenVerifier(String secret, Clock clock) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("El secreto del token debe tener al menos 16 caracteres");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    /** @return el usuario si la firma es correcta y el token no ha vencido. */
    public Optional<AuthenticatedUser> verify(String compact) {
        try {
            String[] parts = compact.split("\\.");
            if (parts.length != 2) {
                return Optional.empty();
            }
            String payload = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
            // MessageDigest.isEqual compara en tiempo constante (evita ataques de temporización)
            boolean validSignature = MessageDigest.isEqual(
                    sign(payload).getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8));
            if (!validSignature) {
                return Optional.empty();
            }
            String[] fields = payload.split("\\|");
            if (!LocalDateTime.parse(fields[2]).isAfter(LocalDateTime.now(clock))) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(fields[0], fields[1], compact));
        } catch (RuntimeException e) {
            return Optional.empty(); // token mal formado
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible verificar el token", e);
        }
    }
}
