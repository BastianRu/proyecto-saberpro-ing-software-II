package com.unicauca.lisw2t2g08.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.unicauca.lisw2t2g08.auth.domain.AuthToken;
import com.unicauca.lisw2t2g08.auth.domain.Role;
import com.unicauca.lisw2t2g08.auth.domain.User;

// Emite y verifica tokens firmados con HMAC-SHA256.
// Formato compacto: base64url(subject|role|expiresAt) + "." + base64url(firma)
// El login de un usuario no puede contener '|' para que el formato sea inequivoco.

public class TokenSigner {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final Duration ttl;
    private final Clock clock;

    public TokenSigner(String secret, Duration ttl, Clock clock) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("El secreto del token debe tener al menos 16 caracteres");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttl = ttl;
        this.clock = clock;
    }

    public AuthToken issue(User user) {
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(ttl).withNano(0);
        String signature = sign(payload(user.getLogin(), user.getRole(), expiresAt));
        return new AuthToken(user.getLogin(), user.getRole(), expiresAt, signature);
    }

    // Cadena que viaja en el header Authorization: Bearer <token>
    public String compact(AuthToken token) {
        String payload = payload(token.getSubject(), token.getRole(), token.getExpiresAt());
        return ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + token.getSignature();
    }

    // Devuelve el token solo si la firma es correcta y no ha vencido.
    public Optional<AuthToken> verify(String compact) {
        try {
            String[] parts = compact.split("\\.");
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
            AuthToken token = new AuthToken(fields[0], Role.valueOf(fields[1]),
                    LocalDateTime.parse(fields[2]), parts[1]);
            return token.isExpired(LocalDateTime.now(clock)) ? Optional.empty() : Optional.of(token);
        } catch (RuntimeException e) {
            return Optional.empty(); // token mal formado
        }
    }

    private static String payload(String subject, Role role, LocalDateTime expiresAt) {
        return subject + "|" + role.name() + "|" + expiresAt;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible firmar el token", e);
        }
    }
}
