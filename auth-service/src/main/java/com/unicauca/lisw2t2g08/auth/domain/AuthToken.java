package com.unicauca.lisw2t2g08.auth.domain;

import java.time.LocalDateTime;

// Token de acceso que reemplaza el retorno directo del User en el login.
// Es inmutable: lleva quien es (subject), su rol, cuando vence y la firma que
// permite a los demas servicios verificarlo sin llamar a este.

public final class AuthToken {

    private final String subject;
    private final Role role;
    private final LocalDateTime expiresAt;
    private final String signature;

    public AuthToken(String subject, Role role, LocalDateTime expiresAt, String signature) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("El subject del token es obligatorio");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol del token es obligatorio");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("La expiración del token es obligatoria");
        }
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("La firma del token es obligatoria");
        }
        this.subject = subject;
        this.role = role;
        this.expiresAt = expiresAt;
        this.signature = signature;
    }

    public String getSubject() {
        return subject;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }
}
